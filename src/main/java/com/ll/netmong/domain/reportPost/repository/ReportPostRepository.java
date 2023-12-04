package com.ll.netmong.domain.reportPost.repository;

import com.ll.netmong.domain.member.entity.Member;
import com.ll.netmong.domain.post.entity.Post;
import com.ll.netmong.domain.reportPost.entity.ReportPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportPostRepository extends JpaRepository<ReportPost, Long> {
    boolean existsByReporterAndReportedPost(Member reporter, Post reportedPost);
}
