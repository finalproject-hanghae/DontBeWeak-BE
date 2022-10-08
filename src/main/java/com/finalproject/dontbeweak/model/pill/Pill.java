package com.finalproject.dontbeweak.model.pill;

import com.finalproject.dontbeweak.model.User;
import com.finalproject.dontbeweak.dto.pill.PillRequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Pill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    //영양제 이름
    @Column(nullable = false)
    private String productName;

    //영양제 색상
    @Column(nullable = false)
    private String customColor;

    //영양제 복용 완료 여부
    @Column(nullable = false)
    private Boolean done;

    @OneToMany
    List<PillHistory> pillHistory;

    public Pill(User user, PillRequestDto pillRequestDto) {
        this.user = user;
        this.productName = pillRequestDto.getProductName();
        this.customColor = pillRequestDto.getCustomColor();
        this.done = false;
    }

    public void donePill() {
        this.done = true;
    }

    public void cancelDonePill() {
        this.done = false;
    }


    public void reset(boolean done){
        this.done = done;
    }
}
