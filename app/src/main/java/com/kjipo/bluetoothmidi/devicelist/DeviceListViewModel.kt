package com.kjipo.bluetoothmidi.devicelist

import androidx.lifecycle.ViewModel

class DeviceListViewModel(val dataSource: DeviceDataSource): ViewModel() {

    val deviceLiveData = dataSource.getDeviceList()

}