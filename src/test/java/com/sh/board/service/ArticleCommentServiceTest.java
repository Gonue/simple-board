package com.sh.board.service;

import com.sh.board.domain.Article;
import com.sh.board.domain.ArticleComment;
import com.sh.board.domain.UserAccount;
import com.sh.board.dto.ArticleCommentDto;
import com.sh.board.dto.UserAccountDto;
import com.sh.board.repository.ArticleCommentRepository;
import com.sh.board.repository.ArticleRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@DisplayName("비즈니스 로직 - 댓글")
@ExtendWith(MockitoExtension.class)
class ArticleCommentServiceTest {
    @InjectMocks private ArticleCommentService sut;
    @Mock private ArticleRepository articleRepository;
    @Mock private ArticleCommentRepository articleCommentRepository;

    @Disabled
    @DisplayName("게시글 ID로 조회시 해당 댓글 리스트 반환")
    @Test
    void givenArticleId_whenSearchingComments_thenReturnsComments(){
        //Given
        Long articleId = 1L;
        ArticleComment expected = createArticleComment("content");
        given(articleCommentRepository.findByArticle_Id(articleId)).willReturn(List.of(expected));
        //When
        List<ArticleCommentDto> actual = sut.searchArticleComment(articleId);
        //Then
        assertThat(actual)
                .hasSize(1)
                .first().hasFieldOrPropertyWithValue("content", expected.getContent());
        then(articleCommentRepository).should().findByArticle_Id(articleId);
    }
    @DisplayName("댓글 정보 입력시 댓글 저장")
    @Test
    void givenArticleCommentInfo_whenSavingArticleComment_thenSavesArticleComment(){
        //Given
        ArticleCommentDto dto = createArticleCommentDto("댓글");
        given(articleRepository.getReferenceById(dto.getArticleId())).willReturn(createArticle());
        given(articleCommentRepository.save(any(ArticleComment.class))).willReturn(null);
        //When
        sut.saveArticleComment(dto);
        //Then
        then(articleRepository).should().getReferenceById(dto.getArticleId());
        then(articleCommentRepository).should().save(any(ArticleComment.class));
    }
    @DisplayName("댓글 저장을 시도했는데 맞는 게시글이 없으면, 경고 로그를 찍고 아무것도 안 한다.")
        @Test
        void givenNonexistentArticle_whenSavingArticleComment_thenLogsSituationAndDoesNothing() {
            // Given
            ArticleCommentDto dto = createArticleCommentDto("댓글");
            given(articleRepository.getReferenceById(dto.getArticleId())).willThrow(EntityNotFoundException.class);

            // When
            sut.saveArticleComment(dto);

            // Then
            then(articleRepository).should().getReferenceById(dto.getArticleId());
            then(articleCommentRepository).shouldHaveNoInteractions();
        }

        @DisplayName("댓글 정보를 입력하면, 댓글을 수정한다.")
        @Test
        void givenArticleCommentInfo_whenUpdatingArticleComment_thenUpdatesArticleComment() {
            // Given
            String oldContent = "content";
            String updatedContent = "댓글";
            ArticleComment articleComment = createArticleComment(oldContent);
            ArticleCommentDto dto = createArticleCommentDto(updatedContent);
            given(articleCommentRepository.getReferenceById(dto.getId())).willReturn(articleComment);

            // When
            sut.updateArticleComment(dto);

            // Then
            assertThat(articleComment.getContent())
                    .isNotEqualTo(oldContent)
                    .isEqualTo(updatedContent);
            then(articleCommentRepository).should().getReferenceById(dto.getId());
        }

        @DisplayName("없는 댓글 정보를 수정하려고 하면, 경고 로그를 찍고 아무 것도 안 한다.")
        @Test
        void givenNonexistentArticleComment_whenUpdatingArticleComment_thenLogsWarningAndDoesNothing() {
            // Given
            ArticleCommentDto dto = createArticleCommentDto("댓글");
            given(articleCommentRepository.getReferenceById(dto.getId())).willThrow(EntityNotFoundException.class);

            // When
            sut.updateArticleComment(dto);

            // Then
            then(articleCommentRepository).should().getReferenceById(dto.getId());
        }

        @DisplayName("댓글 ID를 입력하면, 댓글을 삭제한다.")
        @Test
        void givenArticleCommentId_whenDeletingArticleComment_thenDeletesArticleComment() {
            // Given
            Long articleCommentId = 1L;
            willDoNothing().given(articleCommentRepository).deleteById(articleCommentId);

            // When
            sut.deleteArticleComment(articleCommentId);

            // Then
            then(articleCommentRepository).should().deleteById(articleCommentId);
        }


        private ArticleCommentDto createArticleCommentDto(String content) {
            return ArticleCommentDto.of(
                    1L,
                    1L,
                    createUserAccountDto(),
                    content,
                    LocalDateTime.now(),
                    "kim",
                    LocalDateTime.now(),
                    "kim"
            );
        }

        private UserAccountDto createUserAccountDto() {
            return UserAccountDto.of(
                    1L,
                    "kim",
                    "password",
                    "kim@gmail.com",
                    "Kim",
                    "This is memo",
                    LocalDateTime.now(),
                    "Kim",
                    LocalDateTime.now(),
                    "Kim"
            );
        }

        private ArticleComment createArticleComment(String content) {
            return ArticleComment.of(
                    Article.of(createUserAccount(), "title", "content", "hashtag"),
                    createUserAccount(),
                    content
            );
        }

        private UserAccount createUserAccount() {
            return UserAccount.of(
                    "kim",
                    "password",
                    "kim@gemail.com",
                    "Kim",
                    null
            );
        }

        private Article createArticle() {
            return Article.of(
                    createUserAccount(),
                    "title",
                    "content",
                    "#java"
            );
        }
}
