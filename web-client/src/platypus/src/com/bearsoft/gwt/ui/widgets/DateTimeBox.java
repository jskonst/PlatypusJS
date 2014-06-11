/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bearsoft.gwt.ui.widgets;

import com.bearsoft.gwt.ui.CommonResources;
import com.bearsoft.gwt.ui.XElement;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DatePicker;
import java.util.Date;

/**
 * 
 * @author mg
 */
public class DateTimeBox extends Composite implements RequiresResize, HasValue<Date>, HasValueChangeHandlers<Date>, IsEditor<LeafValueEditor<Date>> {

	private static final DateBox.DefaultFormat DEFAULT_FORMAT = GWT.create(DateBox.DefaultFormat.class);

	protected FlowPanel container = new FlowPanel();
	protected DateBox field;
	protected SimplePanel right = new SimplePanel();
	//
	protected PopupPanel popup = new PopupPanel();

	public DateTimeBox() {
		this(new DatePicker(), null, DEFAULT_FORMAT);
	}

	public DateTimeBox(DatePicker picker, Date date, DateBox.Format format) {
		initWidget(container);
		container.getElement().getStyle().setDisplay(Style.Display.INLINE_BLOCK);
		container.getElement().addClassName("date-time-field");
		field = new DecoratedDateBox(picker, date, format);
		field.setFireNullValues(true);
		field.addValueChangeHandler(new ValueChangeHandler<Date>() {

			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				ValueChangeEvent.fire(DateTimeBox.this, getValue());
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {

					@Override
					public void execute() {
						if (popup.isShowing()) {
							popup.hide();
						}
					}

				});
			}
		});
		field.getElement().getStyle().setDisplay(Style.Display.INLINE_BLOCK);
		// field.getElement().getStyle().setTop(0, Style.Unit.PX);
		// field.getElement().getStyle().setBottom(0, Style.Unit.PX);
		field.getElement().getStyle().setHeight(100, Style.Unit.PCT);
		field.getElement().getStyle().setBorderWidth(0, Style.Unit.PX);
		field.getElement().getStyle().setMargin(0, Style.Unit.PX);
		field.getElement().getStyle().setPadding(0, Style.Unit.PX);

		right.getElement().addClassName("date-time-select");
		right.getElement().getStyle().setDisplay(Style.Display.INLINE_BLOCK);
		right.getElement().getStyle().setTop(0, Style.Unit.PX);
		right.getElement().getStyle().setBottom(0, Style.Unit.PX);
		// right.getElement().getStyle().setHeight(100, Style.Unit.PCT);
		right.getElement().setInnerHTML("&nbsp;");

		CommonResources.INSTANCE.commons().ensureInjected();
		right.getElement().addClassName(CommonResources.INSTANCE.commons().unselectable());

		popup.setStyleName("dateBoxPopup");
		popup.setAutoHideEnabled(true);
		popup.addAutoHidePartner(field.getElement());
		container.add(field);
		container.add(right);
		right.addDomHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				popup.setWidget(field.getDatePicker());
				popup.showRelativeTo(right);
			}
		}, ClickEvent.getType());
	}

	@Override
	public Date getValue() {
		return field.getValue();
	}

	@Override
	public void setValue(Date value) {
		setValue(value, false);
	}

	@Override
	public void setValue(Date aValue, boolean fireEvents) {
		field.setValue(aValue, fireEvents);
	}

	public String getText() {
		return field.getTextBox().getText();
	}

	/**
	 * Gets the format instance used to control formatting and parsing of this
	 * {@link DateBox}.
	 * 
	 * @return the format
	 */
	public DateBox.Format getFormat() {
		return field.getFormat();
	}

	/**
	 * Sets the format used to control formatting and parsing of dates in this
	 * {@link DateBox}. If this {@link DateBox} is not empty, the contents of
	 * date box will be replaced with current contents in the new format.
	 * 
	 * @param format
	 *            the new date format
	 */
	public void setFormat(DateBox.Format format) {
		field.setFormat(format);
	}

	@Override
	public LeafValueEditor<Date> asEditor() {
		return field.asEditor();
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Date> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public void onResize() {
		int containerContentWidth = container.getElement().<XElement> cast().getContentWidth();
		int fieldWidth = field.getElement().getOffsetWidth();
		int rightWidth = right.getElement().getOffsetWidth();
		if (containerContentWidth - fieldWidth - rightWidth != 0) {
			int targetFieldWidth = containerContentWidth - rightWidth;
			field.getElement().getStyle().setWidth(targetFieldWidth, Style.Unit.PX);
			int newContentWidth = field.getElement().getOffsetWidth();
			int delta = newContentWidth - targetFieldWidth;
			if (delta != 0) {
				field.getElement().getStyle().setWidth(targetFieldWidth - delta, Style.Unit.PX);
			}
		}
	}

	@Override
	protected void onAttach() {
		super.onAttach();
	}

	@Override
	protected void onDetach() {
		super.onDetach();
	}

}