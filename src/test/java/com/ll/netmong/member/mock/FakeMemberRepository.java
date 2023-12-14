package com.ll.netmong.member.mock;

import com.ll.netmong.domain.member.entity.AuthLevel;
import com.ll.netmong.domain.member.entity.Member;
import com.ll.netmong.domain.member.entity.ProviderTypeCode;
import com.ll.netmong.domain.member.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FakeMemberRepository implements MemberRepository {

    private final List<Member> data = new ArrayList<>();
    private Long autoGeneratedId = 0L;

    @Override
    public Optional<Member> findById(long id) {
        return data.stream().filter(i->i.getId().equals(id)).findAny();
    }

    @Override
    public Member save(Member member) {
        if (member.getId() == null || member.getId() == 0) {
            Member newUser = Member.builder()
                    .id(autoGeneratedId++)
                    .email(member.getEmail())
                    .realName(member.getRealName())
                    .providerTypeCode(ProviderTypeCode.NETMONG)
                    .authLevel(AuthLevel.MEMBER)
                    .password("encoded"+member.getPassword())
                    .build();
            data.add(newUser);
            return newUser;
        } else {
            data.removeIf(i -> Objects.equals(i.getId(), member.getId()));
            data.add(member);
            return member;
        }
    }

    @Override
    public Optional<Member> findByUsername(String username) {
        return data.stream().filter(i->i.getUsername().equals(username)).findAny();
    }

    @Override
    public Boolean existsByUsername(String username) {
        return data.stream().anyMatch(member -> member.getUsername().equals(username));
    }

    @Override
    public Long countPostsByMemberUsername(String username) {
        return null;
    }
}
