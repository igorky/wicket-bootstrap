package de.agilecoders.wicket.less;

import java.util.concurrent.CountDownLatch;

import org.apache.wicket.Application;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.CssPackageResource;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class LessCacheManagerConcurrentTest
{
    private static final int MAX_RETRIES = 200;
    private WicketTester tester;

    @BeforeEach
    void before()
    {
        tester = new WicketTester(new TestApplication());
    }

    @AfterEach
    void after()
    {
        tester.destroy();
    }

    @Test
    void concurrentModificationIssueInImportedSources() throws Exception
    {
        LessResourceReference lessResRef = new LessResourceReference(getClass(),
                "resources/concurrent.less");

        Class<?> scope = lessResRef.getResource().getScope();
        String name = lessResRef.getName();
        CssPackageResource cpr = new CssPackageResource(scope, name, null, null, null);
        IResourceStream resourceStream = cpr.getResourceStream();

        for (int retries = 0; retries < MAX_RETRIES; retries++)
        {
            LessCacheManager.get().clearCache();

            CountDownLatch latch = new CountDownLatch(1);
            int internalThreadCount = 2;
            Thread[] threads = new Thread[internalThreadCount];
            TestRunnable[] runnables = new TestRunnable[internalThreadCount];

            for (int i = 0; i < internalThreadCount; i++)
            {
                TestRunnable runner = new TestRunnable(tester.getApplication(), latch,
                    resourceStream, scope.getName());
                runnables[i] = runner;

                threads[i] = new Thread(runner, "LessTest_" + i + "_" + retries);
                threads[i].start();
            }

            // Release the waiting threads
            latch.countDown();

            boolean failed = false;
            for (int i = 0; i < internalThreadCount; i++)
            {
                threads[i].join();

                failed = failed || runnables[i].hasFailed();
            }

            assertFalse(failed, "At least one thread reported error");
        }
    }

    private static class TestRunnable implements Runnable
    {
        private WebApplication mApplication;
        private CountDownLatch mLatch;
        private boolean mHasFailed;
        private IResourceStream mResourceStream;
        private String mScopeName;

        TestRunnable(WebApplication application, CountDownLatch latch,
                     IResourceStream resourceStream, String scopeName)
        {
            mApplication = application;
            mLatch = latch;
            mResourceStream = resourceStream;
            mScopeName = scopeName;
            mHasFailed = false;
        }

        @Override
        public void run()
        {
            LessResourceStream lessResourceStream = null;
            try
            {
                if (!Application.exists())
                {
                    ThreadContext.setApplication(mApplication);
                }
                lessResourceStream = new LessResourceStream(mResourceStream, mScopeName);

                // Wait for the latch to be released and then compile the less file
                mLatch.await();
                lessResourceStream.getString();
            }
            catch (Throwable e)
            {
                System.err.println("Thread failed:");
                e.printStackTrace();

                mHasFailed = true;
            }
            finally
            {
                IOUtils.closeQuietly(lessResourceStream);
            }
        }

        boolean hasFailed()
        {
            return mHasFailed;
        }
    }
}
