/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.faces.facelets.tag.ui;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.facelets.FaceletContextImplBase;
import com.sun.faces.facelets.TemplateClient;
import com.sun.faces.facelets.el.VariableMapperWrapper;
import com.sun.faces.facelets.tag.TagHandlerImpl;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagAttributeException;
import jakarta.faces.view.facelets.TagConfig;

import jakarta.el.VariableMapper;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jacob Hookom
 */
public final class DecorateHandler extends TagHandlerImpl implements TemplateClient {

    private static final Logger log = FacesLogger.FACELETS_DECORATE.getLogger();
    
    private final TagAttribute template;

    private final Map handlers;
    
    private final ParamHandler[] params;

    /**
     * @param config
     */
    public DecorateHandler(TagConfig config) {
        super(config);
        this.template = this.getRequiredAttribute("template");
        this.handlers = new HashMap();

        Iterator itr = this.findNextByType(DefineHandler.class);
        DefineHandler d = null;
        while (itr.hasNext()) {
            d = (DefineHandler) itr.next();
            this.handlers.put(d.getName(), d);
            if (log.isLoggable(Level.FINE)) {
                log.fine(tag + " found Define[" + d.getName() + "]");
            }
        }
        List paramC = new ArrayList();
        itr = this.findNextByType(ParamHandler.class);
        while (itr.hasNext()) {
            paramC.add(itr.next());
        }
        if (paramC.size() > 0) {
            this.params = new ParamHandler[paramC.size()];
            for (int i = 0; i < this.params.length; i++) {
                this.params[i] = (ParamHandler) paramC.get(i);
            }
        } else {
            this.params = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sun.facelets.FaceletHandler#apply(com.sun.facelets.FaceletContext,
     *      jakarta.faces.component.UIComponent)
     */
    @Override
    public void apply(FaceletContext ctxObj, UIComponent parent)
            throws IOException {
        FaceletContextImplBase ctx = (FaceletContextImplBase) ctxObj;
        VariableMapper orig = ctx.getVariableMapper();
        if (this.params != null) {
            VariableMapper vm = new VariableMapperWrapper(orig);
            ctx.setVariableMapper(vm);
            for (int i = 0; i < this.params.length; i++) {
                this.params[i].apply(ctx, parent);
            }
        }

        ctx.pushClient(this);
        String path = null;
        try {
            path = this.template.getValue(ctx);
            if (path.trim().length() == 0) {
                throw new TagAttributeException(this.tag, this.template, "Invalid path : " + path);
            }
            WebConfiguration webConfig = WebConfiguration.getInstance(); 
            if (path.startsWith(webConfig.getOptionValue(WebConfiguration.WebContextInitParameter.WebAppContractsDirectory))) {
                throw new TagAttributeException(this.tag, this.template, "Invalid path, contract resources cannot be accessed this way : " + path);
            }
            ctx.includeFacelet(parent, path);
        } catch (IOException e) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, e.toString(), e);
            }
            throw new TagAttributeException(this.tag, this.template, "Invalid path : " + path);
        } finally {
            ctx.setVariableMapper(orig);
            ctx.popClient(this);
        }
    }

    @Override
    public boolean apply(FaceletContext ctx, UIComponent parent, String name) throws IOException {
        if (name != null) {
            DefineHandler handler = (DefineHandler) this.handlers.get(name);
            if (handler != null) {
                handler.applyDefinition(ctx, parent);
                return true;
            } else {
                return false;
            }
        } else {
            this.nextHandler.apply(ctx, parent);
            return true;
        }
    }
}
