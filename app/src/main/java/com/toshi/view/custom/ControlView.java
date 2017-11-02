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

package com.toshi.view.custom;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.toshi.R;
import com.toshi.model.sofa.Control;
import com.toshi.view.BaseApplication;
import com.toshi.view.adapter.ControlAdapter;
import com.toshi.view.adapter.ControlGroupAdapter;

import java.util.ArrayList;
import java.util.List;

public class ControlView extends LinearLayout implements ControlAdapter.OnControlClickListener {

    private OnControlClickedListener listener;

    public ControlView(Context context) {
        super(context);
        init();
    }

    public ControlView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ControlView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public interface OnControlClickedListener {
        void onControlClicked(final Control control);
    }

    public void setOnControlClickedListener(final OnControlClickedListener listener) {
        this.listener = listener;
    }

    private void init() {
        inflate(getContext(), R.layout.control_view, this);
        initControls();
    }

    private void initControls() {
        final ControlAdapter adapter = new ControlAdapter(new ArrayList<>());
        final ControlRecyclerView controlRv = findViewById(R.id.control_recycle_view);
        final int controlSpacing = BaseApplication.get().getResources().getDimensionPixelSize(R.dimen.control_spacing);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false);

        controlRv.setLayoutManager(layoutManager);
        controlRv.setAdapter(adapter);
        controlRv.addItemDecoration(new SpaceDecoration(controlSpacing));
        controlRv.setVisibility(VISIBLE);
        adapter.setControlClickedListener(this);
    }

    public void setOnSizeChangedListener(final ControlRecyclerView.OnSizeChangedListener listener) {
        final ControlRecyclerView controlRv = findViewById(R.id.control_recycle_view);
        controlRv.setOnSizedChangedListener(listener);
    }

    public void showControls(final List<Control> controls) {
        final RecyclerView controlRv = findViewById(R.id.control_recycle_view);
        final ControlAdapter adapter = (ControlAdapter) controlRv.getAdapter();
        controlRv.setVisibility(VISIBLE);
        adapter.setControls(controls);
    }

    private void hideControls() {
        findViewById(R.id.control_recycle_view).setVisibility(GONE);
    }

    private void hideGroupedControlsView() {
        findViewById(R.id.control_grouped_recycle_view_wrapper).setVisibility(GONE);
    }

    public void hideView() {
        hideControls();
        hideGroupedControlsView();
    }

    @Override
    public void onControlClicked(Control control) {
        if (this.listener == null) return;
        this.listener.onControlClicked(control);
    }

    @Override
    public void onGroupedControlItemClicked(final Control control) {
        if (control != null) {
            showGroupedControlsView(control.getControls());
        } else {
            hideGroupedControlsView();
        }
    }

    private void showGroupedControlsView(final List<Control> controls) {
        final RecyclerView controlGroupRv = findViewById(R.id.control_grouped_recycle_view);
        final ControlGroupAdapter adapter = (ControlGroupAdapter) controlGroupRv.getAdapter();

        if (adapter == null) {
            initGroupControlView(controls);
        } else {
            findViewById(R.id.control_grouped_recycle_view_wrapper).setVisibility(VISIBLE);
            adapter.setControls(controls);
        }
    }

    private void initGroupControlView(final List<Control> controls) {
        final RecyclerView controlGroupRv = findViewById(R.id.control_grouped_recycle_view);
        final ControlGroupAdapter adapter = new ControlGroupAdapter(controls);
        final int padding = getResources().getDimensionPixelSize(R.dimen.control_group_recycleview_padding);

        controlGroupRv.setLayoutManager(new LinearLayoutManager(this.getContext()));
        controlGroupRv.setAdapter(adapter);
        controlGroupRv.addItemDecoration(new RecyclerViewDivider(this.getContext(), padding));
        findViewById(R.id.control_grouped_recycle_view_wrapper).setVisibility(VISIBLE);

        adapter.setOnItemClickListener(control -> {
            if (this.listener == null) return;
            this.listener.onControlClicked(control);
        });
    }
}