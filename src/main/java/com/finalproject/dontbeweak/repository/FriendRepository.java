package com.finalproject.dontbeweak.repository;

import com.finalproject.dontbeweak.model.Friend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend,Long> {


    Optional<Friend> findFriendByNickname(String nickname);
}
