package com.ll.netmong.domain.postComment.service;

import com.ll.netmong.domain.postComment.dto.request.PostCommentRequest;
import com.ll.netmong.domain.postComment.dto.response.PostCommentResponse;
import com.ll.netmong.domain.postComment.entity.PostComment;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface PostCommentService {

    PostCommentResponse addPostComment(Long postId, PostCommentRequest postCommentRequest, UserDetails userDetails);

    PostCommentResponse updateComment(Long commentId, PostCommentRequest request, UserDetails userDetails);

    void deleteComment(Long commentId, UserDetails userDetails);

    List<PostCommentResponse> getCommentsOfPost(Long postId);

    PostComment addReplyToComment(Long commentId, PostCommentRequest request, UserDetails userDetails);


    PostComment updateReply(Long replyId, PostCommentRequest request);

}