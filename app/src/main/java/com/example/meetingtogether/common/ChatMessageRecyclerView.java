package com.example.meetingtogether.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.example.meetingtogether.model.MessageDTO;

import java.util.List;

public class ChatMessageRecyclerView {
    private List<?> dataList;
    private RecyclerView recyclerView;
    private Context context;
    private MyRecyclerAdapter adapter;
    public OnItemClickInterface onItemClickInterface;
    public OnBind onBind;
    public int receiveRowItem;
    public int sendRowItem;

    public ChatMessageRecyclerView(OnBind onBind){
        this.onBind = onBind;
    }

    /**
     * ViewBind 와 onBindViewHolder Interface
     */
    public interface OnBind {
        void onBindViewListener(MyRecyclerAdapter.ViewHolder viewHolder, View view, int viewType);
        void onBindViewHolderListener(MyRecyclerAdapter.ViewHolder holder, int position, List<Object> payloads);
        void onLayout(Context context, RecyclerView recyclerView);
    }

    /**
     * Click Item Interface
     */
    public interface OnItemClickInterface{
        void onItemClickListener(View view, int position);
        void onItemLongClickListener(View view, int position);
    }


    public void setContext(Context context){
        this.context = context;
    }

    public void setRecyclerView(RecyclerView recyclerView){
        this.recyclerView = recyclerView;
    }

    public void setDataList(List<?> dataList){
        this.dataList = dataList;
    }

    public int getReceiveRowItem() {
        return receiveRowItem;
    }

    public void setReceiveRowItem(int receiveRowItem) {
        this.receiveRowItem = receiveRowItem;
    }

    public int getSendRowItem() {
        return sendRowItem;
    }

    public void setSendRowItem(int sendRowItem) {
        this.sendRowItem = sendRowItem;
    }

    public void setOnItemClickListener(OnItemClickInterface onItemClickInterface) {
        this.onItemClickInterface = onItemClickInterface;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public MyRecyclerAdapter getAdapter() {
        return adapter;
    }

    public void adapt(){
        // 레이아웃 적용
        onBind.onLayout(context, recyclerView);

        // 어댑터 적용
        adapter = new MyRecyclerAdapter(dataList);
        recyclerView.setAdapter(adapter);
    }

    public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder>{

        private List<?> dataList;

        public MyRecyclerAdapter(List<?> dataList){
            this.dataList = dataList;
        }

        public void updateList(List<?> dataList){
            this.dataList = dataList;
        }

        public List<?> getDataList(){
            return this.dataList;
        }

        public class ViewHolder<T extends ViewBinding, F extends ViewBinding> extends RecyclerView.ViewHolder{

            public T sendBinding;
            public F receiveBinding;

            public ViewHolder(@NonNull View itemView, int viewType) {
                super(itemView);

                onBind.onBindViewListener(this, itemView, viewType);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final int position = getBindingAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            if(onItemClickInterface != null) {
                                onItemClickInterface.onItemClickListener(view, position);
                            }
                        }
                    }
                });
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        final int position = getBindingAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            if(onItemClickInterface != null) {
                                onItemClickInterface.onItemLongClickListener(view, position);
                            }
                        }
                        return false;
                    }
                });
            }

            public void setSendBinding(T binding) {
                this.sendBinding = binding;
            }
            public void setReceiveBinding(F binding) {
                this.receiveBinding = binding;
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View convertView = null;
            if(viewType == MessageDTO.MessageType.RECEIVE.ordinal()){
                convertView = LayoutInflater.from(parent.getContext()).inflate(receiveRowItem, parent,false);
            }else if(viewType == MessageDTO.MessageType.SEND.ordinal()){
                convertView = LayoutInflater.from(parent.getContext()).inflate(sendRowItem, parent,false);
            }
            return new ViewHolder(convertView, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            onBind.onBindViewHolderListener(holder, position, null);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
            onBind.onBindViewHolderListener(holder, position, payloads);
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        @Override
        public int getItemViewType(int position) {
            MessageDTO messageDTO = (MessageDTO) dataList.get(position);
            if(messageDTO.getMessageType() == MessageDTO.MessageType.RECEIVE){
                return MessageDTO.MessageType.RECEIVE.ordinal();
            }else if(messageDTO.getMessageType() == MessageDTO.MessageType.SEND){
                return MessageDTO.MessageType.SEND.ordinal();
            }

            return -1;
        }
    }

}
