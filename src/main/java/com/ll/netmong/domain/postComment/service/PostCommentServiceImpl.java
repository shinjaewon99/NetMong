package com.ll.netmong.domain.postComment.service;

import com.ll.netmong.domain.member.entity.Member;
import com.ll.netmong.domain.member.repository.MemberRepository;
import com.ll.netmong.domain.postComment.dto.response.PostCommentResponse;
import com.ll.netmong.domain.postComment.exception.DataNotFoundException;
import com.ll.netmong.domain.post.entity.Post;
import com.ll.netmong.domain.post.repository.PostRepository;
import com.ll.netmong.domain.postComment.dto.request.PostCommentRequest;
import com.ll.netmong.domain.postComment.entity.PostComment;
import com.ll.netmong.domain.postComment.repository.PostCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostCommentServiceImpl implements PostCommentService {

    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public PostCommentResponse addPostComment(Long postId, PostCommentRequest postCommentRequest, @AuthenticationPrincipal UserDetails userDetails) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new DataNotFoundException("해당하는 게시물을 찾을 수 없습니다."));

        Member member = memberRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new DataNotFoundException("사용자를 찾을 수 없습니다."));

        PostComment comment = PostComment.builder()
                .post(post)
                .memberID(member)
                .username(userDetails.getUsername())
                .content(postCommentRequest.getContent())
                .isDeleted(false)
                .build();
        post.getComments().add(comment);
        PostComment savedComment = postCommentRepository.save(comment);
        return convertToResponse(savedComment);
    }

    @Override
    @Transactional
    public PostComment updateComment(Long commentId, PostCommentRequest request) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new DataNotFoundException("해당 댓글이 없습니다. id=" + commentId));

        comment.update(request.getContent());

        return postCommentRepository.save(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new DataNotFoundException("해당 댓글이 없습니다. id: " + commentId));
        comment.changeIsDeleted(true);
        postCommentRepository.save(comment);
    }

    @Override
    @Transactional
    public List<PostCommentResponse> getCommentsOfPost(Long postId) {
        List<PostComment> comments = postCommentRepository.findByPostIdAndParentCommentIsNull(postId);
        return comments.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    private PostCommentResponse convertToResponse(PostComment comment) {
        List<PostCommentResponse> childResponses = comment.getChildComments() != null ? comment.getChildComments().stream().map(this::convertToResponse).collect(Collectors.toList()) : new ArrayList<>();
        return new PostCommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getIsDeleted(),
                comment.getUsername(),
                comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                childResponses
        );
    }


    @Override
    @Transactional
    public PostComment addReplyToComment(Long commentId, PostCommentRequest request, UserDetails userDetails) {
        PostComment parentComment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new DataNotFoundException("해당 댓글이 없습니다. id: " + commentId));
        Member member = memberRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new DataNotFoundException("해당하는 회원을 찾을 수 없습니다."));
        PostComment childComment = PostComment.builder()
                .content(request.getContent())
                .isDeleted(false)
                .username(String.valueOf(member))
                .build();
        parentComment.addChildComment(childComment);
        return postCommentRepository.save(childComment);
    }

    @Override
    @Transactional
    public List<PostComment> getRepliesOfComment(Long commentId) {
        PostComment parentComment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new DataNotFoundException("해당 댓글이 없습니다. id: " + commentId));
        return parentComment.getChildComments();
    }

    @Override
    @Transactional
    public PostComment updateReply(Long replyId, PostCommentRequest request) {
        PostComment reply = postCommentRepository.findById(replyId)
                .orElseThrow(() -> new DataNotFoundException("해당 대댓글이 없습니다. id: " + replyId));
        reply.update(request.getContent());
        return postCommentRepository.save(reply);
    }
}
