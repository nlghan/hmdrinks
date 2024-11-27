package com.hmdrinks.Service;

import com.hmdrinks.Entity.*;
import com.hmdrinks.Enum.Payment_Method;
import com.hmdrinks.Enum.Status_Order;
import com.hmdrinks.Repository.*;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.BorderRadius;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;

@Service
public class GenerateInvoiceService {
    public final static File fontFile = new File("fonts/vuArial.ttf");
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    static PdfFont vietnameseFont;



    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final PaymentRepository paymentRepository;

    public String formatCurrency(double amount) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(amount);
    }


    public GenerateInvoiceService(OrderRepository orderRepository, UserRepository userRepository, OrderItemRepository orderItemRepository, CartItemRepository cartItemRepository, PaymentRepository paymentRepository) throws IOException {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.paymentRepository = paymentRepository;
    }

    // Helper Methods for Cells
    private static Cell createHeaderTextCell(String textValue) throws IOException {
        vietnameseFont = PdfFontFactory.createFont(fontFile.getAbsolutePath(),PdfEncodings.IDENTITY_H);
        return new Cell().add(new Paragraph(textValue))
                .setBold()
                .setBorder(Border.NO_BORDER)
                .setFont(vietnameseFont)
                .setTextAlignment(TextAlignment.RIGHT);
    }

    private static Cell createHeaderTextCellValue(String textValue) throws IOException {
        vietnameseFont = PdfFontFactory.createFont(fontFile.getAbsolutePath(),PdfEncodings.IDENTITY_H);
        return new Cell().add(new Paragraph(textValue))
                .setBorder(Border.NO_BORDER)
                .setFont(vietnameseFont)
                .setTextAlignment(TextAlignment.RIGHT);
    }

    private static Cell createBillingAndShippingCell(String textValue) throws IOException {
        vietnameseFont = PdfFontFactory.createFont(fontFile.getAbsolutePath(),PdfEncodings.IDENTITY_H);
        return new Cell().add(new Paragraph(textValue))
                .setFontSize(16f)
                .setBold()
                .setBorder(Border.NO_BORDER)
                .setFont(vietnameseFont)
                .setTextAlignment(TextAlignment.LEFT);
    }

    private static Cell createCell10fLeft(String textValue, boolean isBold) throws IOException {
        vietnameseFont = PdfFontFactory.createFont(fontFile.getAbsolutePath(),PdfEncodings.IDENTITY_H);
        Cell cell = new Cell().add(new Paragraph(textValue))
                .setFontSize(12f)
                .setFont(vietnameseFont)
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.LEFT);
        return isBold ? cell.setBold() : cell;
    }

    @Transactional
    public ResponseEntity<?> createInvoice(int orderId) throws IOException {
        vietnameseFont = PdfFontFactory.createFont(fontFile.getAbsolutePath(),PdfEncodings.IDENTITY_H);
        // Initialize PDF Document
        String filePath = "Invoice_" + orderId + ".pdf";
        PdfWriter pdfWriter = new PdfWriter(filePath);
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        pdfDocument.setDefaultPageSize(PageSize.A4);
        Document document = new Document(pdfDocument);

        Orders orders = orderRepository.findByOrderIdAndIsDeletedFalse(orderId);
        if(orders == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found orders");
        }
        if (orders.getStatus() == Status_Order.CANCELLED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order cancel");
        }
        User user = userRepository.findByUserId(orders.getUser().getUserId());
        float[] twoColumnWidth = {230f + 150f, 230f};
        float[] threeColumnWidth = {190f, 190f, 190f};
        float[] forColumnWidth = {190f, 190f, 190f, 190f};
        float[] fullWidth = {230f * 3};

        // Empty space between sections
        Paragraph emptyParagraph = new Paragraph("\n");

        // Tạo một bảng với hai cột
        Table companyInfoTable = new Table(new float[]{100f, 400f}).useAllAvailableWidth(); // Đảm bảo bảng sử dụng hết chiều rộng

// Đường dẫn tới ảnh logo
        String logoPath = "D:\\HK1_nam_4\\tlcn\\HMDRINKS_FE\\hmdrinks_frontend\\src\\assets\\img\\logo.png";

// Tạo đối tượng Image từ ảnh
        Image logoImage = new Image(ImageDataFactory.create(logoPath))
                .scaleToFit(80f, 40f) // Điều chỉnh kích thước ảnh
                .setAutoScale(true); // Tự động căn chỉnh tỷ lệ

// Thêm ảnh vào ô bảng, căn giữa
        Cell logoCell = new Cell()
                .add(logoImage)
                .setBorder(Border.NO_BORDER) // Không hiển thị border
                .setTextAlignment(TextAlignment.CENTER) // Căn giữa theo chiều ngang
                .setVerticalAlignment(VerticalAlignment.MIDDLE); // Căn giữa theo chiều dọc

// Thêm văn bản vào ô bảng
        Cell infoCell = new Cell()
                .add(new Paragraph("Số 1 Võ Văn Ngân, Linh Chiểu, Thủ Đức, Hồ Chí Minh")
                        .setFontSize(10f)
                        .setFont(vietnameseFont))
                .setBorder(Border.NO_BORDER) // Không hiển thị border
                .setTextAlignment(TextAlignment.LEFT) // Căn trái theo chiều ngang
                .setVerticalAlignment(VerticalAlignment.MIDDLE); // Căn giữa theo chiều dọc

// Thêm các ô vào bảng
        companyInfoTable.addCell(logoCell);
        companyInfoTable.addCell(infoCell);

// Thêm bảng vào tài liệu
        document.add(companyInfoTable);

        PdfFont robotoFont = PdfFontFactory.createFont(
                "D:/HK1_nam_4/CNPMM/Cuoi_Ky/Roboto/Roboto-Bold.ttf",
                PdfEncodings.IDENTITY_H,
                PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED
        );


        // Tạo bảng chính với hai cột
        Table mainTable = new Table(twoColumnWidth);
        mainTable.setFixedLayout();
        mainTable.setBorder(Border.NO_BORDER); // Đảm bảo không có viền

// Thêm tiêu đề vào bảng chính


        mainTable.addCell(
                new Cell()
                        .add(new Paragraph("Hóa đơn HMDrinks")
                                .setFontSize(30f)
                                .setFont(robotoFont) // Font Roboto
                                .setFontColor(new DeviceRgb(247, 77, 77))
                                .setTextAlignment(TextAlignment.LEFT))
                        .setBorder(Border.NO_BORDER)
                        .setPadding(0)
        );


// Tạo bảng con với 2 cột cho thông tin hóa đơn
        Table nestedTable = new Table(new float[]{115f, 115f});
        nestedTable.setWidth(200);  // Đảm bảo bảng con chiếm toàn bộ chiều rộng của bảng chính
        nestedTable.setPadding(5f);  // Đặt khoảng cách giữa các ô cho đẹp hơn

// Thêm các dòng thông tin hóa đơn
        nestedTable.addCell(createHeaderTextCell("Số hóa đơn :"));
        nestedTable.addCell(createHeaderTextCellValue(String.valueOf(orderId)));
        nestedTable.addCell(createHeaderTextCell("Ngày in hóa đơn:"));

// In nghiêng ngày và thời gian
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        nestedTable.addCell(createHeaderTextCellValue(LocalDateTime.now().format(formatter)).setItalic());

        mainTable.addCell(new Cell().add(nestedTable).setBorder(Border.NO_BORDER));  // Thêm bảng con vào bảng chính mà không có viền

        mainTable.setBorder(Border.NO_BORDER);  // Đặt viền cho bảng chính là không có viền

// Thêm bảng chính vào tài liệu
        document.add(mainTable);

// Thêm một đoạn trống sau bảng chính (nếu cần thiết)
        document.add(emptyParagraph);

// Tạo một border để phân cách
        Border dividerBorder = new SolidBorder(new DeviceRgb(255, 127, 127), 1f);
        Table divider = new Table(fullWidth);
        divider.setBorder(dividerBorder);
        document.add(divider);

// Thêm đoạn trống giữa các bảng
        document.add(emptyParagraph);

        PdfFont pacificoFont = PdfFontFactory.createFont(
                "D:/HK1_nam_4/CNPMM/Cuoi_Ky/Pacifico/Pacifico-Regular.ttf",
                PdfEncodings.IDENTITY_H,
                PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED
        );


        Table billingShippingTable = new Table(twoColumnWidth);
        billingShippingTable.addCell(
                createBillingAndShippingCell("Thông tin thanh toán")
                        .setFont(pacificoFont)
                        .setFontSize(17f)
                        .setFontColor(new DeviceRgb(0, 147, 135)) // Đặt màu chữ
        );

        billingShippingTable.addCell(
                createBillingAndShippingCell("Thông tin giao hàng")
                        .setFont(pacificoFont)
                        .setFontSize(17f)
                        .setFontColor(new DeviceRgb(0, 147, 135)) // Đặt màu chữ
        );

        document.add(billingShippingTable);

        Table detailsTable = new Table(twoColumnWidth);
        detailsTable.addCell(createCell10fLeft("Hình thức thanh toán", true));
        String text;
        Payment payment = paymentRepository.findByOrderOrderId(orderId);
        if(payment.getPaymentMethod() == Payment_Method.CREDIT)
        {
            text = "Thẻ tín dụng";
        }
        else {
            text = "Tiền mặt";
        }

        detailsTable.addCell(createCell10fLeft("Tên", true));
        detailsTable.addCell(createCell10fLeft(text, false));
        detailsTable.addCell(createCell10fLeft(user.getFullName(), false));
        document.add(detailsTable);


        Table addressTable = new Table(twoColumnWidth);
        addressTable.addCell(createCell10fLeft("Ngày đặt", true));
        addressTable.addCell(createCell10fLeft("Địa chỉ", true));
        addressTable.addCell(createCell10fLeft(String.valueOf(orders.getOrderDate().format(formatter)), false));
        addressTable.addCell(createCell10fLeft(
                user.getStreet() + ", " + user.getWard() + ", " + user.getDistrict() + ", " + user.getCity(), false));

        document.add(addressTable);


        Table additionalInfoTable = new Table(twoColumnWidth);
        additionalInfoTable.addCell(createCell10fLeft("Email", true));
        additionalInfoTable.addCell(createCell10fLeft("Số điện thoại", true));
        additionalInfoTable.addCell(createCell10fLeft(user.getEmail(), false));
        additionalInfoTable.addCell(createCell10fLeft(user.getPhoneNumber(), false));


        document.add(additionalInfoTable.setMarginBottom(10f));

        Table shipperTable = new Table(twoColumnWidth);
        shipperTable.addCell(createCell10fLeft("Người giao hàng", true));
        shipperTable.addCell(createCell10fLeft("Ngày nhận hàng", true));

        shipperTable.addCell(createCell10fLeft(orders.getPayment().getShipment().getUser().getFullName(), false));
        String shipmentDate = orders.getPayment().getShipment().getDateShip() != null
                ? orders.getPayment().getShipment().getDateShip().format(formatter)
                : ""; // Nếu null, đặt giá trị mặc định là chuỗi trống

// Thêm vào bảng
        shipperTable.addCell(createCell10fLeft(shipmentDate, false));
        document.add(shipperTable);
        document.add(emptyParagraph);

        // Product Section


// Tiêu đề bảng sản phẩm
        Table tableDivider1= new Table(fullWidth);
        Border productBorder1 = new SolidBorder(new DeviceRgb(255, 127, 127), 1f);
        document.add(tableDivider1.setBorder(productBorder1));

        Paragraph productTitle1 = new Paragraph("Sản phẩm").setBold().setFont(pacificoFont).setFontSize(17f) .setFontColor(new DeviceRgb(0, 147, 135)) ;// Đặt màu chữ;
        document.add(productTitle1);

// Bảng tiêu đề sản phẩm
        Table productHeaderTable = new Table(4) // Đảm bảo có 4 cột
                .useAllAvailableWidth(); // Đảm bảo bảng sử dụng toàn bộ chiều rộng
        productHeaderTable.setFixedLayout(); // Đảm bảo bảng có layout cố định, không thay đổi kích thước cột
        productHeaderTable.setBackgroundColor(new DeviceRgb(255, 127, 127), 0.7f); // Đổi màu nền header sang #ff7f7f
        productHeaderTable.addCell(new Cell().add(new Paragraph("Mô tả")
                        .setBold().setFontColor(DeviceRgb.WHITE).setFont(vietnameseFont))
                .setBorder(new SolidBorder(1))); // Kẻ viền đều cho header
        productHeaderTable.addCell(new Cell().add(new Paragraph("Số lượng")
                        .setBold().setFontColor(DeviceRgb.WHITE).setTextAlignment(TextAlignment.CENTER).setFont(vietnameseFont))
                .setBorder(new SolidBorder(1)));
        productHeaderTable.addCell(new Cell().add(new Paragraph("Giá (₫)")
                        .setBold().setFontColor(DeviceRgb.WHITE).setTextAlignment(TextAlignment.CENTER).setFont(vietnameseFont))
                .setBorder(new SolidBorder(1)));
        productHeaderTable.addCell(new Cell().add(new Paragraph("Tổng cộng (₫)")
                        .setBold().setFontColor(DeviceRgb.WHITE).setTextAlignment(TextAlignment.RIGHT).setFont(vietnameseFont))
                .setBorder(new SolidBorder(1)));
        document.add(productHeaderTable);

// Lấy dữ liệu sản phẩm
        List<Products> productList = new ArrayList<>();
        OrderItem orderItem = orders.getOrderItem();
        List<CartItem> cartItem = cartItemRepository.findByCart_CartId(orderItem.getCart().getCartId());
        for (CartItem cartItem1 : cartItem) {
            ProductVariants productVariants = cartItem1.getProductVariants();
            productList.add(new Products(productVariants.getProduct().getProName() + "-" + productVariants.getSize(), cartItem1.getQuantity(), productVariants.getPrice()));
        }

// Bảng nội dung sản phẩm
        Table productTable = new Table(4) // Đảm bảo có 4 cột
                .useAllAvailableWidth(); // Đảm bảo bảng sử dụng toàn bộ chiều rộng
        productTable.setFixedLayout(); // Đảm bảo bảng có layout cố định, không thay đổi kích thước cột
        float totalAmount = 0f;
        for (Products product : productList) {
            Double total = product.getQuantity() * product.getPricePerPiece();
            totalAmount += total;
            productTable.addCell(new Cell()
                    .add(new Paragraph(product.getProductName()).setFont(vietnameseFont))
                    .setBorder(new SolidBorder(0.5f))); // Kẻ viền mỏng hơn cho body
            productTable.addCell(new Cell()
                    .add(new Paragraph(Integer.toString(product.getQuantity()))
                            .setTextAlignment(TextAlignment.CENTER))
                    .setBorder(new SolidBorder(0.5f)));
            productTable.addCell(new Cell()
                    .add(new Paragraph(formatCurrency(product.getPricePerPiece())) // Áp dụng định dạng tiền
                            .setTextAlignment(TextAlignment.CENTER))
                    .setBorder(new SolidBorder(0.5f)));
            productTable.addCell(new Cell()
                    .add(new Paragraph(formatCurrency(total) + "₫") // Áp dụng định dạng tiền
                            .setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(new SolidBorder(0.5f)));
        }
        document.add(productTable);





        // Tiêu đề tổng kết thanh toán
        Paragraph totalTitle = new Paragraph("Thanh toán")
                .setBold()
                .setFont(pacificoFont)
                .setFontSize(20f)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(10f)
                .setFontColor(new DeviceRgb(0, 147, 135)); // Đặt màu chữ;
        document.add(totalTitle);

// Tổng kết thanh toán (không dùng bảng)
        Paragraph productFee = new Paragraph("Tiền phí sản phẩm: " + formatCurrency(totalAmount) + "₫")
                .setFont(vietnameseFont)
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(12f)
                .setMarginBottom(5f);
        document.add(productFee);

        Paragraph deliveryFee = new Paragraph("Tiền phí vận chuyển: " + formatCurrency(orders.getDeliveryFee()) + "₫")
                .setFont(vietnameseFont)
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(12f)
                .setMarginBottom(5f);
        document.add(deliveryFee);

        Paragraph discount = new Paragraph("Tiền được giảm giá: " + formatCurrency(orders.getDiscountPrice()) + "₫")
                .setFont(vietnameseFont)
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(12f)
                .setMarginBottom(5f);
        document.add(discount);

// Dòng tổng cộng nổi bật
        double total = totalAmount + orders.getDeliveryFee() - orders.getDiscountPrice();
        if (total <= 0) {
            total = 0;
        }
        Paragraph totalSummary = new Paragraph("Tổng cộng: " + formatCurrency(total) + "₫")
                .setFont(vietnameseFont)
                .setFontSize(15f)
                .setBold()
                .setFontColor(new DeviceRgb(255, 0, 0)) // Màu đỏ nổi bật
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(10f)
                .setMarginBottom(15f);
        document.add(totalSummary);




//        document.add(new Paragraph("\n\n"));
//        document.add(new Paragraph("Chính sách hoàn trả và đổi trả hàng")
//                .setBold().setFontSize(12f).setFont(vietnameseFont).setTextAlignment(TextAlignment.CENTER));
//
//// Return Policy Points
//        document.add(new Paragraph("1. Sản phẩm có thể được hoàn trả trong vòng 7 ngày kể từ ngày nhận hàng.")
//                .setFontSize(10f).setFont(vietnameseFont));
//        document.add(new Paragraph("2. Sản phẩm phải được trả lại trong tình trạng ban đầu, chưa qua sử dụng.")
//                .setFontSize(10f).setFont(vietnameseFont));
//        document.add(new Paragraph("3. Không chấp nhận trả lại hoặc đổi trả với sản phẩm đã qua sử dụng.")
//                .setFontSize(10f).setFont(vietnameseFont));

        document.close();


        File file = new File(filePath);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + file.getName());
        headers.add("Content-Type", "application/pdf");
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        boolean deleted = file.delete();
        if (!deleted) {
            System.err.println("Không thể xóa file: " + file.getAbsolutePath());
        }
        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
    }

    // Product Class
    private static class Products {
        private final String productName;
        private final int quantity;
        private final Double pricePerPiece;

        public Products(String productName, int quantity, Double pricePerPiece) {
            this.productName = productName;
            this.quantity = quantity;
            this.pricePerPiece = pricePerPiece;
        }

        public String getProductName() {
            return productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public Double getPricePerPiece() {
            return pricePerPiece;
        }
    }
}
