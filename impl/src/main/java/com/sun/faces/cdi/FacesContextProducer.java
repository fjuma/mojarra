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

package com.sun.faces.cdi;

import jakarta.enterprise.context.RequestScoped;

import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_3">
 * The FacesContextProducer is the CDI producer that allows injection of the
 * FacesContext using @Inject and allows EL resolving of #{facesContext}
 * </p>
 *
 * @since 2.3
 * @see FacesContext
 */
public class FacesContextProducer extends CdiProducer<FacesContext> {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 1L;

    public FacesContextProducer() {
        super.name("facesContext")
             .scope(RequestScoped.class)
             .types(FacesContext.class)
             .beanClass(FacesContext.class)
             .create(e -> FacesContext.getCurrentInstance());
    }
    
}
