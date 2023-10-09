package com.example.meetingtogether.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.example.meetingtogether.ui.meetings.DTO.MessageModel;

import java.util.List;

public class MessageRecyclerView {
    private List<?> dataList;
    private RecyclerView recyclerView;
    private Context context;
    private MyRecyclerAdapter adapter;
    public OnItemClickInterface onItemClickInterface;
    public OnBind onBind;
    public int receiveRowItem;
    public int sendRowItem;

    public MessageRecyclerView(OnBind onBind){
        this.onBind = onBind;
    }

    /**
     * ViewBind 와 onBindViewHolder Interface
     */
    public interface OnBind {
        void onBindViewListener(MyRecyclerAdapter.ViewHolder viewHolder, View view);
        void onBindViewHolderListener(MyRecyclerAdapter.ViewHolder holder, int position);
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

        public class ViewHolder<T extends ViewBinding> extends RecyclerView.ViewHolder{

            public T binding;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                onBind.onBindViewListener(this, itemView);

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

            public void setBinding(T binding) {
                this.binding = binding;
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View convertView = null;
            if(viewType == MessageModel.MessageType.RECEIVE.getValue()){
                convertView = LayoutInflater.from(parent.getContext()).inflate(receiveRowItem, parent,false);
            }else if(viewType == MessageModel.MessageType.SEND.getValue()){
                convertView = LayoutInflater.from(parent.getContext()).inflate(sendRowItem, parent,false);
            }
            return new ViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            onBind.onBindViewHolderListener(holder, position);
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        @Override
        public int getItemViewType(int position) {
            MessageModel messageModel = (MessageModel) dataList.get(position);
            if(messageModel.getMessageType() == MessageModel.MessageType.RECEIVE){
                return MessageModel.MessageType.RECEIVE.getValue();
            }else if(messageModel.getMessageType() == MessageModel.MessageType.SEND){
                return MessageModel.MessageType.SEND.getValue();
            }

            return -1;
        }
    }

}
