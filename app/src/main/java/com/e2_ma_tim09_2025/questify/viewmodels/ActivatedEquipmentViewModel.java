package com.e2_ma_tim09_2025.questify.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.models.MyEquipment;
import com.e2_ma_tim09_2025.questify.services.UserService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ActivatedEquipmentViewModel extends ViewModel {

    private final UserService userService;
    private final MutableLiveData<List<MyEquipment>> activatedEquipment = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    @Inject
    public ActivatedEquipmentViewModel(UserService userService) {
        this.userService = userService;
    }

    public void loadActivatedEquipment() {
        String userId = userService.getCurrentUserId();
        if (userId == null) {
            errorMessage.setValue("User not logged in");
            return;
        }

        isLoading.setValue(true);
        
        // Get user's equipment from their profile
        userService.getUser(userId, task -> {
            isLoading.setValue(false);
            
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                com.e2_ma_tim09_2025.questify.models.User user = task.getResult().toObject(com.e2_ma_tim09_2025.questify.models.User.class);
                if (user != null && user.getEquipment() != null) {
                    // Filter only activated equipment
                    List<MyEquipment> activatedList = new ArrayList<>();
                    for (MyEquipment equipment : user.getEquipment()) {
                        if (equipment.isActivated()) {
                            activatedList.add(equipment);
                        }
                    }
                    activatedEquipment.setValue(activatedList);
                } else {
                    activatedEquipment.setValue(new ArrayList<>());
                }
            } else {
                errorMessage.setValue("Error loading activated equipment");
                activatedEquipment.setValue(new ArrayList<>());
            }
        });
    }

    public LiveData<List<MyEquipment>> getActivatedEquipment() {
        return activatedEquipment;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
