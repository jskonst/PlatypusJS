package com.eas.client.form.grid.columns;

import java.text.ParseException;

import com.bearsoft.gwt.ui.widgets.ObjectFormat;
import com.bearsoft.gwt.ui.widgets.grid.cells.CellRenderer;
import com.bearsoft.gwt.ui.widgets.grid.cells.TreeExpandableCell;
import com.bearsoft.rowset.Row;
import com.eas.client.application.PlatypusImageResource;
import com.eas.client.converters.ObjectRowValueConverter;
import com.eas.client.form.ControlsUtils;
import com.eas.client.form.grid.RenderedCellContext;
import com.eas.client.form.grid.cells.PlatypusFormattedObjectEditorCell;
import com.eas.client.form.published.PublishedCell;
import com.eas.client.form.published.PublishedStyle;
import com.eas.client.form.published.widgets.model.ModelFormattedField;
import com.eas.client.form.published.widgets.model.PublishedDecoratorBox;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class FormattedObjectModelGridColumn extends ModelGridColumn<Object> {

	public FormattedObjectModelGridColumn(String aName) {
		super(new TreeExpandableCell<Row, Object>(new PlatypusFormattedObjectEditorCell()), aName, null, null, new ObjectRowValueConverter());
		setEditor(new ModelFormattedField());
		((PlatypusFormattedObjectEditorCell) getTargetCell()).setRenderer(new CellRenderer<Object>() {
			@Override
			public boolean render(com.google.gwt.cell.client.Cell.Context context, Object value, SafeHtmlBuilder sb) {
				FormattedObjectModelGridColumn column = FormattedObjectModelGridColumn.this;
				JavaScriptObject onRender = column.getOnRender() != null ? column.getOnRender() : column.getGrid().getOnRender();
				if (onRender != null) {
					try {
						PublishedStyle styleToRender = null;
						SafeHtmlBuilder lsb = new SafeHtmlBuilder();
						String toRender = ((PlatypusFormattedObjectEditorCell) getTargetCell()).getFormat().format(value);
						PublishedCell cellToRender = calcContextPublishedCell(column.getPublished(), onRender, context, column.getColumnModelRef(), toRender, column.getRowsEntity());
						if (cellToRender != null) {
							styleToRender = cellToRender.getStyle();
							if (cellToRender.getDisplay() != null)
								toRender = cellToRender.getDisplay();
						}
						if (toRender == null)
							lsb.append(SafeHtmlUtils.fromTrustedString("&#160;"));
						else
							lsb.append(SafeHtmlUtils.fromString(toRender));
						styleToRender = column.getGrid().complementPublishedStyle(styleToRender);
						String decorId = ControlsUtils.renderDecorated(lsb, styleToRender, sb);
						if (cellToRender != null) {
							if(context instanceof RenderedCellContext){
								((RenderedCellContext)context).setStyle(styleToRender);
							}
							FormattedObjectModelGridColumn.this.bindDisplayCallback(decorId, cellToRender);		
							if(cellToRender.getStyle() != null && cellToRender.getStyle().getIcon() instanceof PlatypusImageResource){
								PlatypusImageResource pImage = (PlatypusImageResource)cellToRender.getStyle().getIcon();
								FormattedObjectModelGridColumn.this.bindIconCallback(decorId, pImage);
							}
						}
					} catch (Exception e) {
						sb.append(SafeHtmlUtils.fromString(e.getMessage()));
					}
					return true;
				} else
					return false;
			}
		});
	}

	@Override
	public void setEditor(PublishedDecoratorBox<Object> aEditor) {
		super.setEditor(aEditor);
		((PlatypusFormattedObjectEditorCell) getTargetCell()).setEditor(aEditor);
	}

	public String getFormat() {
		ObjectFormat format = ((PlatypusFormattedObjectEditorCell) getTargetCell()).getFormat();
		return format != null ? format.getPattern() : null;
	}

	public void setFormat(String aValue) throws ParseException {
		ObjectFormat format = ((PlatypusFormattedObjectEditorCell) getTargetCell()).getFormat();
		if (format != null) {
			format.setPattern(aValue);
		}
		((ModelFormattedField) getEditor()).setFormat(aValue);
	}
	
	public void setFormatType(int aType, String aPattern) throws ParseException {
		((PlatypusFormattedObjectEditorCell) getTargetCell()).setFormat(new ObjectFormat(aType, aPattern));
		((ModelFormattedField) getEditor()).setFormatType(aType, aPattern);
	}
	
	public String getEmptyText(){
		return ((ModelFormattedField)getEditor()).getEmptyText();
	}
	
	public void setEmptyText(String aValue) {
		((ModelFormattedField)getEditor()).setEmptyText(aValue);
    }
}
