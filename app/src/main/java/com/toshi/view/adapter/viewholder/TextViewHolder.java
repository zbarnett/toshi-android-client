/*
 * 	Copyright (c) 2017. Toshi Browser, Inc
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

package com.toshi.view.adapter.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.toshi.R;
import com.toshi.model.local.SendState;
import com.toshi.util.ImageUtil;
import com.toshi.view.adapter.listeners.OnItemClickListener;
import com.vdurmont.emoji.EmojiManager;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public final class TextViewHolder extends RecyclerView.ViewHolder {

    private @NonNull TextView message;
    private @NonNull TextView emojiMessage;
    private @Nullable CircleImageView avatar;
    private @Nullable ImageView sentStatus;

    private String text;
    private @SendState.State int sendState;
    private String avatarUri;

    public TextViewHolder(final View v) {
        super(v);
        this.message = (TextView) v.findViewById(R.id.message);
        this.emojiMessage = (TextView) v.findViewById(R.id.emoji_message);
        this.avatar = (CircleImageView) v.findViewById(R.id.avatar);
        this.sentStatus = (ImageView) v.findViewById(R.id.sent_status);
    }

    public TextViewHolder setText(final String text) {
        this.text = text;
        return this;
    }

    public TextViewHolder setAvatarUri(final String uri) {
        this.avatarUri = uri;
        return this;
    }

    public TextViewHolder setSendState(final @SendState.State int sendState) {
        this.sendState = sendState;
        return this;
    }

    public TextViewHolder draw() {
        renderText();
        renderAvatar();
        setSendState();
        return this;
    }

    private void renderText() {
        if (this.text == null) return;

        if (EmojiManager.isOnlyEmojis(this.text)) {
            this.message.setVisibility(View.GONE);
            this.emojiMessage.setVisibility(View.VISIBLE);
            this.emojiMessage.setText(this.text);
        } else {
            this.emojiMessage.setVisibility(View.GONE);
            this.message.setVisibility(View.VISIBLE);
            this.message.setText(this.text);
        }
    }

    private void renderAvatar() {
        if (this.avatar == null) return;
        ImageUtil.load(this.avatarUri, this.avatar);
    }

    private void setSendState() {
        if (this.sentStatus == null) return;
        final int visibility = this.sendState == SendState.STATE_FAILED || this.sendState == SendState.STATE_PENDING
                ? View.VISIBLE
                : View.GONE;
        this.sentStatus.setVisibility(visibility);
    }

    public void setClickableUsernames(final OnItemClickListener<String> listener) {
        if (this.text == null) {
            return;
        }

        final SpannableString spannableString = new SpannableString(this.text);
        int lastEndPos = 0;

        for (final String word : getUsernames()) {
            final int currentStartPos = this.text.indexOf(word, lastEndPos);
            final int currentEndPos = this.text.indexOf(word, lastEndPos) + word.length();

            spannableString.setSpan(new ClickableSpan() {
                @Override
                public void onClick(final View view) {
                   handleSpannedClicked(view, listener, this);
                }
            }, currentStartPos,
               currentEndPos,
               Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            lastEndPos = currentEndPos;
        }

        this.message.setText(spannableString);
        this.message.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private List<String> getUsernames() {
        final Pattern pattern = Pattern.compile("(?:^|\\s)@(\\w+)");
        final Matcher matcher = pattern.matcher(this.text);
        final List<String> matches = new ArrayList<>();

        while (matcher.find()) {
            matches.add(matcher.group(1));
        }

        return matches;
    }

    private void handleSpannedClicked(final View view,
                                      final OnItemClickListener<String> listener,
                                      final ClickableSpan clickableSpan) {
        final TextView tv = (TextView) view;
        final Spanned spannedString = (Spanned) tv.getText();
        final String username =
                spannedString
                        .subSequence(
                                spannedString.getSpanStart(clickableSpan),
                                spannedString.getSpanEnd(clickableSpan))
                        .toString();

        listener.onItemClick(username);
    }
}
