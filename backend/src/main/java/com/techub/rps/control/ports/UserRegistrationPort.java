package com.techub.rps.control.ports;

import com.techub.rps.control.model.User;

public interface UserRegistrationPort {
    User registerUser(String username);
    boolean usernameExists(String username);
}
