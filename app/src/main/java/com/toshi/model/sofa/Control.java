/*
 * 	Copyright (c) 2017. Toshi Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.toshi.model.sofa;

import java.util.List;

public class Control {
    private final static String WEBVIEW_ACTION = "Webview::";

    private String type;
    private String label;
    private String value;
    private String action;
    private List<Control> controls;

    public String getType() {
        return this.type;
    }

    public String getLabel() {
        return this.label;
    }

    public String getValue() {
        return this.value;
    }

    public String getAction() {
        return this.action;
    }

    public List<Control> getControls() {
        return this.controls;
    }

    public boolean hasAction() {
        return this.action != null;
    }

    // This currently is naive and assumed the action is a URL.
    // This is the only action we currently support.
    public String getActionUrl() {
        return this.action.replace(WEBVIEW_ACTION, "");
    }
}
