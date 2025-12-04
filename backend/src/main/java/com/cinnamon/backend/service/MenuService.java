package com.cinnamon.backend.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class MenuService {

    public List<Object> getMenuItems() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection("menuItems").get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream().map(QueryDocumentSnapshot::getData).collect(Collectors.toList());
    }
}
