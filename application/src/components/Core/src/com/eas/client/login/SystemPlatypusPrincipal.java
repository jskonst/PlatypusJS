/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.client.login;

import com.bearsoft.rowset.utils.IDGenerator;
import com.eas.script.NoPublisherException;
import java.util.function.Consumer;
import jdk.nashorn.api.scripting.JSObject;

/**
 *
 * @author mg
 */
public class SystemPlatypusPrincipal extends PlatypusPrincipal {

    public SystemPlatypusPrincipal() {
        super("system-" + IDGenerator.genID());
    }

    @Override
    public boolean hasRole(String aRole, Consumer<Boolean> onSuccess, Consumer<Exception> onFailure) throws Exception {
        if (onSuccess != null) {
            onSuccess.accept(Boolean.TRUE);
        }
        return true;
    }

    @Override
    public String getStartAppElement(Consumer<String> onSuccess, Consumer<Exception> onFailure) throws Exception {
        if (onSuccess != null) {
            onSuccess.accept(null);
        }
        return null;
    }

    @Override
    public Object getPublished() {
        if (published == null) {
            if (publisher == null || !publisher.isFunction()) {
                throw new NoPublisherException();
            }
            published = publisher.call(null, new Object[]{this});
        }
        return published;
    }

    private static JSObject publisher;

    public static void setPublisher(JSObject aPublisher) {
        publisher = aPublisher;
    }
}
