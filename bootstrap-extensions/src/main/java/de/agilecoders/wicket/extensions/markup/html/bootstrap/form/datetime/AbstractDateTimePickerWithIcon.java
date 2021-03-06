package de.agilecoders.wicket.extensions.markup.html.bootstrap.form.datetime;

import de.agilecoders.wicket.core.markup.html.bootstrap.image.GlyphIconType;
import de.agilecoders.wicket.core.markup.html.bootstrap.image.Icon;
import de.agilecoders.wicket.core.markup.html.bootstrap.image.IconType;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;

/**
 * Datetime-picker with calendar icon.
 *
 * @author Alexey Volkov
 * @since 01.02.2015
 */
public abstract class AbstractDateTimePickerWithIcon<T> extends FormComponentPanel<T> {

    private static final long serialVersionUID = 1L;

    private DatetimePickerConfig config;

    /**
     * Construct.
     *
     * @param markupId markup id
     * @param config   DateTimePicker config
     */
    public AbstractDateTimePickerWithIcon(String markupId, DatetimePickerConfig config) {
        this(markupId, null, config);
    }

    /**
     * Construct.
     *
     * @param markupId markup id
     * @param model    model
     * @param config   DateTimePicker config
     */
    public AbstractDateTimePickerWithIcon(String markupId, IModel<T> model, DatetimePickerConfig config) {
        super(markupId, model);
        setRenderBodyOnly(true);
        this.config = config;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        Component input = newInput("date", config.getFormat()).add(new DatetimePickerBehavior(config));
        if (config.getMaskInput()) {
            input.add(config.newMaskBehavior());
        }

        add(new WebMarkupContainer("dateWrapper")
                .add(
                    newInput("date", config.getFormat()),
                    newIcon("icon")
                )
                .add(
                    new DatetimePickerBehavior(config)
                )
        );
    }

    /**
     * use specified config
     *
     * @param config config to use
     * @return current instance
     */
    public AbstractDateTimePickerWithIcon<T> with(DatetimePickerConfig config) {
        this.config = config;
        return this;
    }

    /**
     * get current config for tweaking
     *
     * @return current config
     */
    public DatetimePickerConfig getConfig() {
        return config;
    }

    /**
     * @param wicketId   wicket id
     * @param dateFormat datetime format
     * @return new input text field
     */
    abstract protected Component newInput(String wicketId, String dateFormat);

    /**
     * @param wicketId wicket id
     * @return icon component
     */
    protected Component newIcon(String wicketId) {
        return new Icon(wicketId, newIconType());
    }

    /**
     * @return icon type
     */
    protected IconType newIconType() {
        return GlyphIconType.calendar;
    }
}
