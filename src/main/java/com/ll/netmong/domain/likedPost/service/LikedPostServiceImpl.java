package com.ll.netmong.domain.likedPost.service;

import com.ll.netmong.domain.member.entity.Member;
import com.ll.netmong.domain.member.repository.MemberRepository;
import com.ll.netmong.domain.post.entity.Post;
import com.ll.netmong.domain.post.repository.PostRepository;
import com.ll.netmong.domain.postComment.exception.DataNotFoundException;
import com.ll.netmong.domain.likedPost.dto.request.LikedPostRequest;
import com.ll.netmong.domain.likedPost.dto.response.LikedPostResponse;
import com.ll.netmong.domain.likedPost.entity.LikedPost;
import com.ll.netmong.domain.likedPost.repository.LikedPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikedPostServiceImpl implements LikedPostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final LikedPostRepository likedPostRepository;

    // TODO: 로직 분리
    // TODO: 스트림, 람다 사용해서 좋아요 중복 및 취소 확인

    @Override
    @Transactional
    public void addLike(Post post, @AuthenticationPrincipal UserDetails userDetails) {
        Member member = getMemberById(userDetails);

        LikedPost like = LikedPost.builder()
                .post(post)
                .member(member)
                .build();

        post.addLike(like);
        likedPostRepository.save(like);
    }

    @Override
    @Transactional
    public int countLikes(Post post) {
        return likedPostRepository.countLikesByPost(post);
    }

    @Override
    @Transactional
    public Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new DataNotFoundException("해당하는 게시물을 찾을 수 없습니다."));
    }

    @Override
    @Transactional
    public Member getMemberById(@AuthenticationPrincipal UserDetails userDetails) {
        return memberRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new DataNotFoundException("사용자를 찾을 수 없습니다."));
    }
}
