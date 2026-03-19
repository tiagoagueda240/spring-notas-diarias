package com.tiagoagueda.api.journal.repository;

import com.tiagoagueda.api.journal.entity.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TagRepositoryDataJpaTest {

    @Autowired
    private TagRepository tagRepository;

    @Test
    void findByNameIgnoreCase_WhenTagExists_ReturnsTagIgnoringCase() {
        tagRepository.save(Tag.builder().name("Backend").build());

        Optional<Tag> foundTag = tagRepository.findByNameIgnoreCase("backend");

        assertThat(foundTag).isPresent();
        assertThat(foundTag.get().getName()).isEqualTo("Backend");
    }
}
