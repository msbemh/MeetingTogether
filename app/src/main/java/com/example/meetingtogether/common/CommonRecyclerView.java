package com.example.meetingtogether.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import java.util.List;

public class CommonRecyclerView {
    private List<?> dataList;
    private RecyclerView recyclerView;
    private Context context;
    private MyRecyclerAdapter adapter;
    public OnItemClickInterface onItemClickInterface;
    public OnBind onBind;
    public int rowItem;

    public CommonRecyclerView(OnBind onBind){
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

    public void setRowItem(int rowItem){
        this.rowItem = rowItem;
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
            View convertView = LayoutInflater.from(parent.getContext()).inflate(rowItem, parent,false);
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
    }

}
