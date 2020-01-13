package org.desperu.go4lunch.view.adapter;

import org.desperu.go4lunch.view.base.BaseAdapter;
import org.desperu.go4lunch.viewmodel.MessageViewModel;

import java.util.List;

public class ChatAdapter extends BaseAdapter {

    // FOR DATA
    private final int layoutId;
    private List<MessageViewModel> messageList;

    public ChatAdapter(int layoutId, List<MessageViewModel> messageList) {
        this.layoutId = layoutId;
        this.messageList = messageList;
    }

    @Override
    protected Object getObjForPosition(int position) { return this.messageList.get(position); }

    @Override
    protected Object getObj2ForPosition(int position) { return null; }

    @Override
    protected int getLayoutIdForPosition(int position) { return layoutId; }

    @Override
    public int getItemCount() { return this.messageList.size(); }
}