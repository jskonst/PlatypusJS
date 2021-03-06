/* global Java */

define(['boxing'], function(B) {
    var className = "com.eas.client.model.application.EntityInstanceInsertEvent";
    var javaClass = Java.type(className);
    /**
     * Generated constructor.
     * @constructor EntityInstanceInsertEvent EntityInstanceInsertEvent
     */
    function EntityInstanceInsertEvent() {
        var maxArgs = 0;
        var delegate = arguments.length > maxArgs ?
              arguments[maxArgs] 
            : new javaClass();

        Object.defineProperty(this, "unwrap", {
            configurable: true,
            value: function() {
                return delegate;
            }
        });
        if(EntityInstanceInsertEvent.superclass)
            EntityInstanceInsertEvent.superclass.constructor.apply(this, arguments);
        delegate.setPublished(this);
        /**
         * The inserted element.
         */
        this.inserted = new Object();
        Object.defineProperty(this, "inserted", {
            get: function() {
                var value = delegate.inserted;
                return value;
            }
        });

        /**
         * The source object of the event.
         */
        this.source = new Object();
        Object.defineProperty(this, "source", {
            get: function() {
                var value = delegate.source;
                return B.boxAsJs(value);
            }
        });

        /**
         * The inserted element.
         */
        this.object = new Object();
        Object.defineProperty(this, "object", {
            get: function() {
                var value = delegate.object;
                return value;
            }
        });

    }

    var ScriptsClass = Java.type("com.eas.script.Scripts");
    var space = ScriptsClass.getSpace();
    space.putPublisher(className, function(aDelegate) {
        return new EntityInstanceInsertEvent(aDelegate);
    });
    return EntityInstanceInsertEvent;
});