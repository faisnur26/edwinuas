package com.project.edwinuas_nasmoco.api;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<String> nama = new MutableLiveData<>();

    public void setNama(String nama) {
        this.nama.setValue(nama);
    }

    public LiveData<String> getNama() {
        return nama;
    }
}