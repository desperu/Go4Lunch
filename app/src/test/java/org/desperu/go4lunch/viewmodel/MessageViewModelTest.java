package org.desperu.go4lunch.viewmodel;

import androidx.test.core.app.ApplicationProvider;

import org.desperu.go4lunch.models.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest=Config.NONE)
public class MessageViewModelTest {

    @Test
    public void testPlacesListLiveData() {
        ArrayList<Message> allMessageList = new ArrayList<>();

        MessageViewModel messageViewModel = new MessageViewModel(ApplicationProvider.getApplicationContext());
        messageViewModel.setAllMessageListLiveData(allMessageList);

        messageViewModel.getAllMessageListLiveData().observeForever(allMessageList1 -> {
            assertNotNull(allMessageList1);
            assertEquals(allMessageList, allMessageList1);
        });
    }
}