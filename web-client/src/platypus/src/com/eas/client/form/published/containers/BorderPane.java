package com.eas.client.form.published.containers;

import com.bearsoft.gwt.ui.XElement;
import com.bearsoft.gwt.ui.containers.BorderPanel;
import com.eas.client.form.EventsExecutor;
import com.eas.client.form.published.HasComponentPopupMenu;
import com.eas.client.form.published.HasEventsExecutor;
import com.eas.client.form.published.HasJsFacade;
import com.eas.client.form.published.HasPublished;
import com.eas.client.form.published.menu.PlatypusPopupMenu;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.touch.client.Point;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;

public class BorderPane extends BorderPanel implements HasJsFacade, HasEnabled, HasComponentPopupMenu, HasEventsExecutor {

	protected EventsExecutor eventsExecutor;
	protected PlatypusPopupMenu menu;
	protected boolean enabled = true;
	protected String name;
	protected JavaScriptObject published;

	public BorderPane() {
		super();
	}

	public BorderPane(int aVGap, int aHGap) {
		this();
		setHgap(aHGap);
		setVgap(aVGap);
	}

	@Override
	public EventsExecutor getEventsExecutor() {
		return eventsExecutor;
	}

	@Override
	public void setEventsExecutor(EventsExecutor aExecutor) {
		eventsExecutor = aExecutor;
	}

	@Override
	public PlatypusPopupMenu getPlatypusPopupMenu() {
		return menu;
	}

	protected HandlerRegistration menuTriggerReg;

	@Override
	public void setPlatypusPopupMenu(PlatypusPopupMenu aMenu) {
		if (menu != aMenu) {
			if (menuTriggerReg != null)
				menuTriggerReg.removeHandler();
			menu = aMenu;
			if (menu != null) {
				menuTriggerReg = super.addDomHandler(new ContextMenuHandler() {
					
					@Override
					public void onContextMenu(ContextMenuEvent event) {
						event.preventDefault();
						event.stopPropagation();
						menu.setPopupPosition(event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY());
						menu.show();
					}
				}, ContextMenuEvent.getType());
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean aValue) {
		boolean oldValue = enabled;
		enabled = aValue;
		if(!oldValue && enabled){
			getElement().<XElement>cast().unmask();
		}else if(oldValue && !enabled){
			getElement().<XElement>cast().disabledMask();
		}
	}

	@Override
	public String getJsName() {
		return name;
	}

	@Override
	public void setJsName(String aValue) {
		name = aValue;
	}

	public void add(Widget aWidget, Direction aDirection, Point aSize) {
		Direction d = getResolvedDirection(aDirection);
		switch (d) {
		case WEST:
			setLeftComponent(aWidget, aSize.getX());
		case EAST:
			setRightComponent(aWidget, aSize.getX());
		case NORTH:
			setTopComponent(aWidget, aSize.getY());
		case SOUTH:
			setBottomComponent(aWidget, aSize.getY());
		case CENTER:
			setCenterComponent(aWidget);
		}
	}

	public Widget getLeftComponent() {
		for (Widget w : getChildren()) {
			Direction d = getResolvedDirection(getWidgetDirection(w));
			if (d == Direction.WEST) {
				return w;
			}
		}
		return null;
	}

	public void setLeftComponent(Widget w, double size) {
		Widget old = getLeftComponent();
		if (old != w) {
			if (old != null)
				remove(old);
			addWest(w, size);
			super.getCenter();
		}
	}

	public Widget getRightComponent() {
		for (Widget w : getChildren()) {
			Direction d = getResolvedDirection(getWidgetDirection(w));
			if (d == Direction.EAST) {
				return w;
			}
		}
		return null;
	}

	public void setRightComponent(Widget w, double size) {
		Widget old = getRightComponent();
		if (old != w) {
			if (old != null)
				remove(old);
			addEast(w, size);
		}
	}

	public Widget getTopComponent() {
		for (Widget w : getChildren()) {
			Direction d = getResolvedDirection(getWidgetDirection(w));
			if (d == Direction.NORTH) {
				return w;
			}
		}
		return null;
	}

	public void setTopComponent(Widget w, double size) {
		Widget old = getTopComponent();
		if (old != w) {
			if (old != null)
				remove(old);
			addNorth(w, size);
		}
	}

	public Widget getBottomComponent() {
		for (Widget w : getChildren()) {
			Direction d = getResolvedDirection(getWidgetDirection(w));
			if (d == Direction.SOUTH) {
				return w;
			}
		}
		return null;
	}

	public void setBottomComponent(Widget w, double size) {
		Widget old = getBottomComponent();
		if (old != w) {
			if (old != null)
				remove(old);
			addSouth(w, size);
		}
	}

	public Widget getCenterComponent() {
		return getCenter();
	}

	public void setCenterComponent(Widget w) {
		Widget old = getCenter();
		if (old != w) {
			if (old != null)
				remove(old);
			super.add(w);
		}
	}

	@Override
	public JavaScriptObject getPublished() {
		return published;
	}

	@Override
	public void setPublished(JavaScriptObject aValue) {
		if (published != aValue) {
			published = aValue;
			if (published != null) {
				publish(this, aValue);
			}
		}
	}

	private native static void publish(HasPublished aWidget, JavaScriptObject published)/*-{
		Object.defineProperty(published, "leftComponent", {
			get : function() {
				var comp = aWidget.@com.eas.client.form.published.containers.BorderPane::getLeftComponent()();
				return @com.eas.client.form.Publisher::checkPublishedComponent(Ljava/lang/Object;)(comp);
			},
			set : function(aChild) {
				if (aChild != null) {
				 	aWidget.@com.eas.client.form.published.containers.BorderPane::setLeftComponent(Lcom/google/gwt/user/client/ui/Widget;D)(aChild.unwrap(), toAdd.width);
				}else
					published.remove(published.leftComponent);
			}
		});
		Object.defineProperty(published, "topComponent", {
			get : function() {
				var comp = aWidget.@com.eas.client.form.published.containers.BorderPane::getTopComponent()();
				return @com.eas.client.form.Publisher::checkPublishedComponent(Ljava/lang/Object;)(comp);
			},
			set : function(aChild) {
				if (aChild != null) {
				 	aWidget.@com.eas.client.form.published.containers.BorderPane::setTopComponent(Lcom/google/gwt/user/client/ui/Widget;D)(aChild.unwrap(), toAdd.height);
				}else
					published.remove(published.topComponent);
			}
		});
		Object.defineProperty(published, "rightComponent", {
			get : function() {
				var comp = aWidget.@com.eas.client.form.published.containers.BorderPane::getRightComponent()();
				return @com.eas.client.form.Publisher::checkPublishedComponent(Ljava/lang/Object;)(comp);
			},
			set : function(aChild) {
				if (aChild != null) {
				 	aWidget.@com.eas.client.form.published.containers.BorderPane::setRightComponent(Lcom/google/gwt/user/client/ui/Widget;D)(aChild.unwrap(), toAdd.width);
				}else
					published.remove(published.rightComponent);
			}
		});
		Object.defineProperty(published, "bottomComponent", {
			get : function() {
				var comp = aWidget.@com.eas.client.form.published.containers.BorderPane::getBottomComponent()();
				return @com.eas.client.form.Publisher::checkPublishedComponent(Ljava/lang/Object;)(comp);
			},
			set : function(aChild) {
				if (aChild != null) {
				 	aWidget.@com.eas.client.form.published.containers.BorderPane::setBottomComponent(Lcom/google/gwt/user/client/ui/Widget;D)(aChild.unwrap(), toAdd.height);
				}else
					published.remove(published.bottomComponent);
			}
		});
		Object.defineProperty(published, "centerComponent", {
			get : function() {
				var comp = aWidget.@com.eas.client.form.published.containers.BorderPane::getCenterComponent()();
				return @com.eas.client.form.Publisher::checkPublishedComponent(Ljava/lang/Object;)(comp);
			},
			set : function(aChild) {
				if (aChild != null) {
				 	aWidget.@com.eas.client.form.published.containers.BorderPane::setCenterComponent(Lcom/google/gwt/user/client/ui/Widget;)(aChild.unwrap());
				}else
					published.remove(published.centerComponent);
			}
		});
		published.add = function(toAdd, region, aSize) {
			if(toAdd != undefined && toAdd != null && toAdd.unwrap != undefined){
				if(toAdd.parent == published)
					throw 'A widget already added to this container';
				if(!region){
					region = $wnd.P.VerticalPosition.CENTER;
				}
				if(published.centerComponent){
					throw 'No widget can be added after center widget';
				}
				switch (region) {
					case $wnd.P.VerticalPosition.CENTER:
						aWidget.@com.eas.client.form.published.containers.BorderPane::add(Lcom/google/gwt/user/client/ui/Widget;)(toAdd.unwrap());
						break;  
					case $wnd.P.VerticalPosition.TOP: 
						if (!aSize) {
							aSize = toAdd.height;
							if (!aSize) {
								aSize = 32;
							}
						}
						aWidget.@com.eas.client.form.published.containers.BorderPane::addNorth(Lcom/google/gwt/user/client/ui/Widget;D)(toAdd.unwrap(), aSize);
						break;  
					case $wnd.P.VerticalPosition.BOTTOM: 
						if (!aSize) {
							aSize = toAdd.height;
							if (!aSize) {
								aSize = 32;
							}
						}
						aWidget.@com.eas.client.form.published.containers.BorderPane::addSouth(Lcom/google/gwt/user/client/ui/Widget;D)(toAdd.unwrap(), aSize);
						break;  
					case $wnd.P.HorizontalPosition.LEFT: 
						if (!aSize) {
							aSize = toAdd.width;
							if (!aSize) {
								aSize = 32
							}
						}
						aWidget.@com.eas.client.form.published.containers.BorderPane::addWest(Lcom/google/gwt/user/client/ui/Widget;D)(toAdd.unwrap(), aSize);
						break;  
					case $wnd.P.HorizontalPosition.RIGHT: 
						if (!aSize) {
							aSize = toAdd.width;
							if (!aSize) {
								aSize = 32
							}
						}
						aWidget.@com.eas.client.form.published.containers.BorderPane::addEast(Lcom/google/gwt/user/client/ui/Widget;D)(toAdd.unwrap(), aSize);
						break;  
				}
			}
		}
	}-*/;
}