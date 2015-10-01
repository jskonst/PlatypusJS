package com.eas.client.application.js;

import com.bearsoft.gwt.ui.containers.window.events.MoveEvent;
import com.eas.client.form.events.ActionEvent;
import com.eas.client.form.events.AddEvent;
import com.eas.client.form.events.HideEvent;
import com.eas.client.form.events.RemoveEvent;
import com.eas.client.form.events.ShowEvent;
import com.eas.client.form.published.PublishedCell;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;

public class JsEvents {

	public native static void init()/*-{
		function predefine(aDeps, aName, aDefiner){
			var resolved = [];
			for(var d = 0; d < aDeps.length; d++){
				var module = @com.eas.client.application.Application::prerequire(Ljava/lang/String;)(aDeps[d]);
				resolved.push(module);
			}
			@com.eas.client.application.Application::predefine(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(aName, aDefiner(resolved));
		}
		
		predefine([], 'core/published-sourced-event', function(){
			function PublishedSourcedEvent(aSource){
				Object.defineProperty(this, "source", {
					get : function(){
						return aSource;
					}
				});
			}
			return PublishedSourcedEvent;
		});		
		predefine([], 'datamodel/cursor-position-will-change-event', function(){
			function CursorPositionWillChangeEvent(aSource, aOldIndex, aNewIndex){
				Object.defineProperty(this, "source", {
					get : function(){
						return aSource;
					}
				});
				Object.defineProperty(this, "oldIndex", {
					get : function(){
						return aOldIndex;
					}
				});
				Object.defineProperty(this, "newIndex", {
					get : function(){
						return aNewIndex;
					}
				});
			}
			return CursorPositionWillChangeEvent;
		});
		
		predefine([], 'datamodel/cursor-position-changed-event', function(){
			function CursorPositionChangedEvent(aSource, aOldIndex, aNewIndex){
				Object.defineProperty(this, "source", {
					get : function(){
						return aSource;
					}
				});
				Object.defineProperty(this, "oldIndex", {
					get : function(){
						return aOldIndex;
					}
				});
				Object.defineProperty(this, "newIndex", {
					get : function(){
						return aNewIndex;
					}
				});
			}
			return CursorPositionChangedEvent;
		});
		
		predefine([], 'datamodel/entity-instance-change-event', function(){
			function EntityInstanceChangeEvent(aSource, aField, aOldValue, aNewValue){
				Object.defineProperty(this, "source", {
					get : function(){
						return aSource;
					}
				});
				Object.defineProperty(this, "object", {
					get : function(){
						return aSource;
					}
				});
				Object.defineProperty(this, "field", {
					get : function(){
						return aField;
					}
				});
				Object.defineProperty(this, "oldValue", {
					get : function(){
						return aOldValue;
					}
				});
				Object.defineProperty(this, "newValue", {
					get : function(){
						return aNewValue;
					}
				});
			}
			return EntityInstanceChangeEvent;
		});
		
		predefine([], 'datamodel/entity-instance-delete-event', function(){
			function EntityInstanceDeleteEvent(aSource, aDeleted){
				Object.defineProperty(this, "source", {
					get : function(){
						return aSource;
					}
				});
				Object.defineProperty(this, "deleted", {
					get : function(){
						return aDeleted;
					}
				});
			}
			return EntityInstanceDeleteEvent;
		});
		
		predefine([], 'datamodel/entity-instance-insert-event', function(){
			function EntityInstanceInsertEvent(aSource, aInserted){
				Object.defineProperty(this, "source", {
					get : function(){
						return aSource;
					}
				});
				Object.defineProperty(this, "inserted", {
					get : function(){
						return aInserted;
					}
				});
				Object.defineProperty(this, "object", {
					get : function(){
						return aInserted;
					}
				});
			}
			return EntityInstanceInsertEvent;
		});
		
		predefine([], 'forms/cell-render-event', function(){
			function CellRenderEvent(aSource, aRowId, aColumnId, aRendered, aCell){
				Object.defineProperty(this, "source", {
					get : function(){
						return aSource;
					}
				});
				Object.defineProperty(this, "id", {
					get : function(){
						return aRowId;
					}
				});
				Object.defineProperty(this, "columnId", {
					get : function(){
						return aColumnId;
					}
				});
				Object.defineProperty(this, "object", {
					get : function(){
						return aRendered;
					}
				});
				Object.defineProperty(this, "cell", {
					get : function(){
						return aCell;
					}
				});
			}
			return CellRenderEvent;
		});
		
		predefine([], 'forms/window-event', function(){
			function WindowEvent(aSource){
				Object.defineProperty(this, "source", {
					get : function(){
						return aSource;
					}
				});
			}
			return WindowEvent;
		});
				
		predefine([], 'forms/mouse-event', function(){
			function MouseEvent(aEvent, aClickCount){
				Object.defineProperty(this, "source", {
					get : function() {
						var source = aEvent.@com.google.web.bindery.event.shared.Event::getSource()();
						var jsSource = @com.eas.client.form.Publisher::checkPublishedComponent(Ljava/lang/Object;)(source);
						return jsSource;
					}
				});
				Object.defineProperty(this, "x", {
					get : function() {
						return aEvent.@com.google.gwt.event.dom.client.MouseEvent::getX()();
					}
				});
				Object.defineProperty(this, "y", {
					get : function() {
						return aEvent.@com.google.gwt.event.dom.client.MouseEvent::getY()();
					}
				});
				Object.defineProperty(this, "screenX", {
					get : function() {
						aEvent.@com.google.gwt.event.dom.client.MouseEvent::getScreenX()();
					}
				});
				Object.defineProperty(this, "screenY", {
					get : function() {
						aEvent.@com.google.gwt.event.dom.client.MouseEvent::getScreenY()();
					}
				});
				Object.defineProperty(this, "altDown", {
					get : function() {
						return aEvent.@com.google.gwt.event.dom.client.MouseEvent::isAltKeyDown()();
					}
				});
				Object.defineProperty(this, "controlDown", {
					get : function() {
						return aEvent.@com.google.gwt.event.dom.client.MouseEvent::isControlKeyDown()();
					}
				});
				Object.defineProperty(this, "shiftDown", {
					get : function() {
						return aEvent.@com.google.gwt.event.dom.client.MouseEvent::isShiftKeyDown()();
					}
				});
				Object.defineProperty(this, "metaDown", {
					get : function() {
						return aEvent.@com.google.gwt.event.dom.client.MouseEvent::isMetaKeyDown()();
					}
				});
				Object.defineProperty(this, "button", {
					get : function() {
						var button = aEvent.@com.google.gwt.event.dom.client.MouseEvent::getNativeButton()();
						switch (button) {
							case @com.google.gwt.dom.client.NativeEvent::BUTTON_LEFT : return 1; 
							case @com.google.gwt.dom.client.NativeEvent::BUTTON_RIGHT : return 2; 
							case @com.google.gwt.dom.client.NativeEvent::BUTTON_MIDDLE : return 3;
							default : return 0;
						} 
					}
				});
				Object.defineProperty(this, "clickCount", {
					get : function() {
						if(aClickCount)
							return aClickCount;
						else 
							return 0;
					}
				});
			}
			return MouseEvent;
		});
		
		predefine([], 'forms/key-event', function(){
			function KeyEvent(aEvent){
				Object.defineProperty(this, "source", {
					get : function() {
						var source = aEvent.@com.google.web.bindery.event.shared.Event::getSource()();
						var jsSource = @com.eas.client.form.Publisher::checkPublishedComponent(Ljava/lang/Object;)(source);
						return jsSource;
					}
				});
				Object.defineProperty(this, "altDown", {
					get : function() {
						return aEvent.@com.google.gwt.event.dom.client.KeyEvent::isAltKeyDown()();
					}
				});
				Object.defineProperty(this, "controlDown", {
					get : function() {
						return aEvent.@com.google.gwt.event.dom.client.KeyEvent::isControlKeyDown()();
					}
				});
				Object.defineProperty(this, "shiftDown", {
					get : function() {
						return aEvent.@com.google.gwt.event.dom.client.KeyEvent::isShiftKeyDown()();
					}																																																																			
				});
				Object.defineProperty(this, "metaDown", {
					get : function() {
						return aEvent.@com.google.gwt.event.dom.client.KeyEvent::isMetaKeyDown()();
					}
				});
				Object.defineProperty(this, "key", {
					get : function() {																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																
						var ne = aEvent.@com.google.gwt.event.dom.client.KeyEvent::getNativeEvent()();
						return ne.keyCode || 0;																																																																																																																																																																																																																																																																																																																																																																																																																																																																													
					}
	            });
				Object.defineProperty(this, "char", {
					get : function() {																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																
						var ne = aEvent.@com.google.gwt.event.dom.client.KeyEvent::getNativeEvent()();
						return String.fromCharCode(ne.charCode || 0); 
					}
				});
			}
			return KeyEvent;
		});
		
		predefine([], 'forms/container-event', function(){
			function ContainerEvent(aEvent, isAdd){
				Object.defineProperty(this, "source", {
					get : function() {
						var source = aEvent.@com.google.web.bindery.event.shared.Event::getSource()();
						var jsSource = @com.eas.client.form.Publisher::checkPublishedComponent(Ljava/lang/Object;)(source);
						return jsSource;
					}
				});
				Object.defineProperty(this, "child", {
					get : function(){
						var comp;
						if(isAdd)
							comp = aEvent.@com.eas.client.form.events.AddEvent::getWidget()();
						else
							comp = aEvent.@com.eas.client.form.events.RemoveEvent::getWidget()();
						return @com.eas.client.form.Publisher::checkPublishedComponent(Ljava/lang/Object;)(comp);
					}
				});
			}
			return ContainerEvent;
		});
		
		predefine([], 'forms/item-event', function(){
			function ItemEvent(aSource, aItem){
				Object.defineProperty(this, "source", {
					get : function(){
						return aSource;
					}
				});
				Object.defineProperty(this, "item", {
					get : function(){
						return aItem;
					}
				});
			}
			return ItemEvent;
		});
		
		predefine([], 'forms/component-event', function(){
			function ComponentEvent(aEvent){
				Object.defineProperty(this, "source", {
					get : function() {
						var source = aEvent.@com.google.web.bindery.event.shared.Event::getSource()();
						var jsSource = @com.eas.client.form.Publisher::checkPublishedComponent(Ljava/lang/Object;)(source);
						return jsSource;
					}
				});
			}
			return ComponentEvent;
		});
		
		predefine([], 'forms/focus-event', function(){
			function FocusEvent(aEvent){
				Object.defineProperty(this, "source", {
					get : function() {
						var source = aEvent.@com.google.web.bindery.event.shared.Event::getSource()();
						var jsSource = @com.eas.client.form.Publisher::checkPublishedComponent(Ljava/lang/Object;)(source);
						return jsSource;
					}
				});
			}
			return FocusEvent;
		});
		
		predefine([], 'forms/action-event', function(){
			function ActionEvent(aEvent){
				Object.defineProperty(this, "source", {
					get : function() {
						var source = aEvent.@com.google.web.bindery.event.shared.Event::getSource()();
						var jsSource = @com.eas.client.form.Publisher::checkPublishedComponent(Ljava/lang/Object;)(source);
						return jsSource;
					}
				});
			}
			return ActionEvent;
		});
	}-*/;
	
	public native static JavaScriptObject getFormsClass()/*-{
		return $wnd.P.Form;
	}-*/;
	
	public native static JavaScriptObject publishSourcedEvent(JavaScriptObject aSource)/*-{
		return new $wnd.P.PublishedSourcedEvent(aSource);
	}-*/;	
	
	public native static JavaScriptObject publishItemEvent(JavaScriptObject aSource, JavaScriptObject aItem)/*-{
		return new $wnd.P.ItemEvent(aSource, aItem);
	}-*/;	

	public native static JavaScriptObject publishCursorPositionWillChangeEvent(JavaScriptObject aSource, int aOldIndex, int aNewIndex)/*-{
		return new $wnd.P.CursorPositionWillChangeEvent(aSource, aOldIndex, aNewIndex);
	}-*/;	
	
	public native static JavaScriptObject publishCursorPositionChangedEvent(JavaScriptObject aSource, int aOldIndex, int aNewIndex)/*-{
		return new $wnd.P.CursorPositionChangedEvent(aSource, aOldIndex, aNewIndex);
	}-*/;		
	
	public native static JavaScriptObject publishEntityInstanceChangeEvent(JavaScriptObject aSource, JavaScriptObject aField, Object aOldValue, Object aNewValue)/*-{
		return new $wnd.P.EntityInstanceChangeEvent(aSource, aField, $wnd.P.boxAsJs(aOldValue), $wnd.P.boxAsJs(aNewValue));
	}-*/;
	
	public native static JavaScriptObject publishEntityInstanceDeleteEvent(JavaScriptObject aSource, JavaScriptObject aDeleted)/*-{
		return new $wnd.P.EntityInstanceDeleteEvent(aSource, aDeleted);
	}-*/;
	
	public native static JavaScriptObject publishEntityInstanceInsertEvent(JavaScriptObject aSource, JavaScriptObject aInserted)/*-{
		return new $wnd.P.EntityInstanceInsertEvent(aSource, aInserted);
	}-*/;

	public native static JavaScriptObject publishOnRenderEvent(JavaScriptObject aSource, Object aRowId, Object aColumnId, JavaScriptObject aRendered, PublishedCell aCell)/*-{
		return new $wnd.P.CellRenderEvent(aSource, $wnd.P.boxAsJs(aRowId), $wnd.P.boxAsJs(aColumnId), aRendered, aCell);
	}-*/;
	
	public native static JavaScriptObject publishWindowEvent(Object aEvent, JavaScriptObject aWindow)/*-{
		return new $wnd.P.WindowEvent(aWindow);
	}-*/;

	public native static JavaScriptObject publish(MouseDownEvent aEvent)/*-{
		return new $wnd.P.MouseEvent(aEvent);
	}-*/;

	public native static JavaScriptObject publish(MouseUpEvent aEvent)/*-{
		return new $wnd.P.MouseEvent(aEvent);
	}-*/;

	public native static JavaScriptObject publish(MouseWheelEvent aEvent)/*-{
		return new $wnd.P.MouseEvent(aEvent);
	}-*/;

	public native static JavaScriptObject publish(MouseMoveEvent aEvent)/*-{
		return new $wnd.P.MouseEvent(aEvent);
	}-*/;

	public native static JavaScriptObject publish(ClickEvent aEvent)/*-{
		return new $wnd.P.MouseEvent(aEvent, 1);
	}-*/;

	public native static JavaScriptObject publish(DoubleClickEvent aEvent)/*-{
		return new $wnd.P.MouseEvent(aEvent, 2);
	}-*/;

	public native static JavaScriptObject publish(MouseOverEvent aEvent)/*-{
		return new $wnd.P.MouseEvent(aEvent);
	}-*/;
 
	public native static JavaScriptObject publish(MouseOutEvent aEvent)/*-{
		return new $wnd.P.MouseEvent(aEvent);
	}-*/;
	
	public native static JavaScriptObject publish(KeyDownEvent aEvent)/*-{
		return new $wnd.P.KeyEvent(aEvent);
	}-*/;

	public native static JavaScriptObject publish(KeyUpEvent aEvent)/*-{
		return new $wnd.P.KeyEvent(aEvent);
	}-*/;
	
	public native static JavaScriptObject publish(KeyPressEvent aEvent)/*-{
		return new $wnd.P.KeyEvent(aEvent);
	}-*/;
	
	public native static JavaScriptObject publish(FocusEvent aEvent)/*-{
		return new $wnd.P.FocusEvent(aEvent);
	}-*/;

	public native static JavaScriptObject publish(BlurEvent aEvent)/*-{
		return new $wnd.P.FocusEvent(aEvent);
	}-*/;

	public native static JavaScriptObject publish(ResizeEvent aEvent)/*-{
		return new $wnd.P.ComponentEvent(aEvent);
	}-*/;

	public native static JavaScriptObject publish(ShowEvent aEvent)/*-{
		return new $wnd.P.ComponentEvent(aEvent);
	}-*/;

	public native static JavaScriptObject publish(HideEvent aEvent)/*-{
		return new $wnd.P.ComponentEvent(aEvent);
	}-*/;

	public native static JavaScriptObject publish(MoveEvent<Object> aEvent)/*-{
		return new $wnd.P.ComponentEvent(aEvent);
	}-*/;

	public native static JavaScriptObject publish(AddEvent aEvent)/*-{
		return new $wnd.P.ContainerEvent(aEvent, true); 
	}-*/;

	public native static JavaScriptObject publish(RemoveEvent aEvent)/*-{
		return new $wnd.P.ContainerEvent(aEvent, false);
	}-*/;
	
	public native static JavaScriptObject publish(ActionEvent aEvent)/*-{
		return new $wnd.P.ActionEvent(aEvent);
	}-*/;

}
