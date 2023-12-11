package com.ll.netmong.domain.post.service;

import com.ll.netmong.common.PermissionDeniedException;
import com.ll.netmong.domain.likedPost.repository.LikedPostRepository;
import com.ll.netmong.domain.member.entity.Member;
import com.ll.netmong.domain.member.repository.MemberRepository;
import com.ll.netmong.domain.post.dto.request.PostRequest;
import com.ll.netmong.domain.post.dto.response.PostResponse;
import com.ll.netmong.domain.post.entity.Post;
import com.ll.netmong.domain.post.repository.PostRepository;
import com.ll.netmong.domain.postComment.exception.DataNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final LikedPostRepository likedPostRepository;
    private final MemberRepository memberRepository;

    @Override
    public Page<PostResponse> searchPostsByCategory(String category, String searchWord, Pageable pageable) {
        Map<String, BiFunction<String, Pageable, Page<Post>>> searchByCategory = new HashMap<>(); //BiFunction<String, Pageable, Page<Post>> - String, Pageable 매개변수를 받아 Page<Post> 반환
        searchByCategory.put("작성자", (word, page) -> postRepository.findByWriterContaining(word, page));
        searchByCategory.put("내용", (word, page) -> postRepository.findByContentContaining(word, page));

        BiFunction<String, Pageable, Page<Post>> searchingPosts = searchByCategory.getOrDefault(category, (word, page) -> Page.empty());
        Page<Post> posts = searchingPosts.apply(searchWord, pageable);

        return posts.map(PostResponse::postsView);
    }

    @Override
    public Page<PostResponse> viewPostsByPage(Pageable pageable) {
        Page<Post> postsPage = postRepository.findAll(pageable);

        return postsPage.map(PostResponse::postsView);
    }

    @Override
    @Transactional
    public Post uploadPost(PostRequest postRequest, Member foundMember, String foundUsername) {
        return postRepository.save(Post.builder()
                .title(postRequest.getTitle())
                .member(foundMember)
                .writer(foundUsername)
                .content(postRequest.getContent())
                .imageUrl(postRequest.getImageUrl()).build());
    }

    @Override
    public PostResponse getDetail(long id, UserDetails userDetails) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("포스트를 찾을 수 없습니다."));

        Member member = memberRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new DataNotFoundException("사용자를 찾을 수 없습니다."));
        boolean isLiked = likedPostRepository.existsByMemberAndPost(member, post);

        PostResponse postResponse = new PostResponse(post);
        postResponse.setIsLiked(isLiked);

        return postResponse;
    }

    @Override
    @Transactional
    public void deletePost(Long postId, String foundUsername) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("포스트를 찾을 수 없습니다."));

        if (post.getWriter().equals(foundUsername)) {
            postRepository.deleteById(postId);
        } else {
            throw new PermissionDeniedException("해당 포스트에 대한 삭제 권한이 없습니다.");
        }
    }

    @Override
    @Transactional
    public void updatePost(Long id, PostRequest updatedPostRequest, String foundUsername) {
        Post originPost = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("포스트를 찾을 수 없습니다."));

        if (originPost.getWriter().equals(foundUsername)) {
            Post updatedPost = originPost.toBuilder()
                    .title(updatedPostRequest.getTitle())
                    .content(updatedPostRequest.getContent())
                    .imageUrl(updatedPostRequest.getImageUrl()).build();

            postRepository.save(updatedPost);
        } else {
            throw new PermissionDeniedException("해당 포스트에 대한 수정 권한이 없습니다.");
        }
    }

    @Override
    public List<PostResponse> viewMyPosts(Long memberId) {
        List<Post> posts = postRepository.findByMemberIdAndDeleteDateIsNullOrderByCreateDateDesc(memberId);

        return posts.stream()
                .map(PostResponse::new)
                .toList();
    }
}
