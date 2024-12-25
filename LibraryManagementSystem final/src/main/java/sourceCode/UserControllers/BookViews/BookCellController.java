package sourceCode.UserControllers.BookViews;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import sourceCode.Models.Book;

import static sourceCode.LoginController.imageCache;
import static sourceCode.LoginController.imagedefault;

public class BookCellController {

    @FXML
    private ImageView bookImage;
    @FXML
    private Label titleLabel;
    @FXML
    private Label authorLabel;
    @FXML
    private Label isbnLabel;
    @FXML
    private TextArea descriptionTextArea;

    public void setBook(Book book) {
        titleLabel.setText(book.getTitle());
        authorLabel.setText("Author: " + book.getAuthor());
        isbnLabel.setText("ISBN: " + book.getISBN());
        descriptionTextArea.setText("Description:" + '\n' + book.getDescription());
        if (book.getImageUrl() != null) {
            if (imageCache.containsKey(book.getImageUrl())) {
                // Nếu ảnh đã có trong cache, sử dụng ảnh đó
                bookImage.setImage(imageCache.get(book.getImageUrl()));
            } else {
                try {
                    // Tải ảnh trực tiếp trong luồng chính
                    Image img = new Image(book.getImageUrl());
                    if (!img.isError()) {
                        // Nếu tải thành công, lưu vào cache và hiển thị ảnh
                        imageCache.put(book.getImageUrl(), img);
                        bookImage.setImage(img);
                        System.out.println("Image loaded and cached: " + book.getImageUrl());
                    } else {
                        // Nếu ảnh có lỗi, sử dụng ảnh mặc định
                        System.out.println("Image error, using default for URL: " + book.getImageUrl());
                        bookImage.setImage(imagedefault);
                    }
                } catch (Exception e) {
                    // Xử lý ngoại lệ và sử dụng ảnh mặc định
                    System.out.println("Exception while loading image: " + book.getImageUrl());
                    e.printStackTrace();
                    bookImage.setImage(imagedefault);
                }
            }
        }
    }
}
