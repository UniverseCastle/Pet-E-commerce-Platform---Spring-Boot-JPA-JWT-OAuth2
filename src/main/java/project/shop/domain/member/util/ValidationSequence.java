package project.shop.domain.member.util;

import jakarta.validation.GroupSequence;

import static project.shop.domain.member.util.ValidationGroups.*;

@GroupSequence({NotBlankGroup.class, PatternGroup.class, SizeGroup.class})
public interface ValidationSequence {
}