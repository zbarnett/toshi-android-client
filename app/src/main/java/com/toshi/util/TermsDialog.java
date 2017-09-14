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

package com.toshi.util;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.toshi.R;

public class TermsDialog {

    private AlertDialog dialog;

    public TermsDialog(final Context context, final View.OnClickListener listener) {
        final LinearLayout dialogView =  (LinearLayout) LayoutInflater.from(context).inflate(R.layout.view_terms, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(dialogView);

        final TextView message = dialogView.findViewById(R.id.message);
        message.setMovementMethod(LinkMovementMethod.getInstance());

        final TextView cancel = dialogView.findViewById(R.id.cancel);
        cancel.setOnClickListener(view -> this.dialog.dismiss());

        final TextView agree = dialogView.findViewById(R.id.agree);
        agree.setOnClickListener(view -> {
            this.dialog.dismiss();
            listener.onClick(view);
        });

        this.dialog = builder.create();
        this.dialog.show();
    }

    public void show() {
        if (this.dialog == null) return;
        this.dialog.show();
    }

    public void dismiss() {
        if (this.dialog == null) return;
        this.dialog.dismiss();
    }
}
