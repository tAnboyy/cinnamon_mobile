package com.cinnamon.backend.service;

import com.cinnamon.backend.model.RegistrationRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public String registerUser(RegistrationRequest registrationRequest) throws FirebaseAuthException {
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(registrationRequest.email())
                .setPassword(registrationRequest.password())
                .setDisplayName(registrationRequest.displayName());

        UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
        return userRecord.getUid();
    }
}
