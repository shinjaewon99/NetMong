package com.ll.netmong.domain.post.entity;

import com.ll.netmong.common.BaseEntity;
import com.ll.netmong.domain.member.entity.Member;
import com.ll.netmong.domain.postComment.entity.PostComment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@SuperBuilder(toBuilder = true)
public class Post extends BaseEntity {
    @Column(length=100)
    private String title;
    private String writer;
    @Column(length=100)
    private String content;
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    //포스트 수정
    public void update(String content, String imageUrl) {
        this.content = content;
        this.imageUrl = imageUrl;
    }

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<PostComment> comments = new ArrayList<>();

    public void addComment(PostComment comment) {
        this.comments.add(comment);
        comment.setPost(this);
    }
}
