-- Drop the cross-module foreign keys from the reviews table
ALTER TABLE reviews DROP FOREIGN KEY fk_review_book;
ALTER TABLE reviews DROP FOREIGN KEY fk_review_user_username;

-- Drop the cross-module foreign keys from the book_loans table
ALTER TABLE book_loans DROP FOREIGN KEY fk_loan_book_id;
ALTER TABLE book_loans DROP FOREIGN KEY fk_loan_user_username;