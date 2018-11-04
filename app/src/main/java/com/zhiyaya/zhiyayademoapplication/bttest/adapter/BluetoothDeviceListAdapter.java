package com.zhiyaya.zhiyayademoapplication.bttest.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.zhiyaya.zhiyayademoapplication.R;

import java.util.List;

/**
 * Created by yerunjie on 2018/11/4
 *
 * @author yerunjie
 */
public class BluetoothDeviceListAdapter extends RecyclerView.Adapter<BluetoothDeviceListAdapter.DeviceHolder> implements View.OnClickListener {

    private List<BluetoothDevice> bluetoothDevices;
    private Context context;
    private OnItemClickListener mOnItemClickListener = null;

    public BluetoothDeviceListAdapter(List<BluetoothDevice> bluetoothDevices, Context context) {
        this.bluetoothDevices = bluetoothDevices;
        this.context = context;
    }

    @Override
    public DeviceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bluetooth_device, parent, false);
        view.setOnClickListener(this);
        return new DeviceHolder(view);
    }

    @Override
    public void onBindViewHolder(DeviceHolder holder, int position) {
        holder.bindData(position);
    }

    @Override
    public int getItemCount() {
        return bluetoothDevices.size();
    }

    @Override
    public void onClick(View view) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取position
            mOnItemClickListener.onItemClick(view, bluetoothDevices.get((int) view.getTag()));
        }
    }

    class DeviceHolder extends RecyclerView.ViewHolder {
        TextView tv_device_name;
        TextView tv_device_mac;

        public DeviceHolder(View itemView) {
            super(itemView);
            tv_device_name = itemView.findViewById(R.id.tv_device_name);
            tv_device_mac = itemView.findViewById(R.id.tv_device_mac);
        }

        public void bindData(int position) {
            itemView.setTag(position);
            BluetoothDevice bluetoothDevice = bluetoothDevices.get(position);
            tv_device_name.setText(bluetoothDevice.getName());
            tv_device_mac.setText(bluetoothDevice.getAddress());
        }
    }


    public interface OnItemClickListener {
        void onItemClick(View view, BluetoothDevice item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }
}
