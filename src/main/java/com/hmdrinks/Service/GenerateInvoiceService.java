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

@Service
public class GenerateInvoiceService {
    public final static File fontFile = new File("fonts/vuArial.ttf");
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    static PdfFont vietnameseFont;



    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final PaymentRepository paymentRepository;

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

    public ResponseEntity<?> createInvoice(int orderId) throws IOException {
        vietnameseFont = PdfFontFactory.createFont(fontFile.getAbsolutePath(),PdfEncodings.IDENTITY_H);
        // Initialize PDF Document
        String filePath = "Invoice_" + orderId + ".pdf";
        PdfWriter pdfWriter = new PdfWriter(filePath);
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        pdfDocument.setDefaultPageSize(PageSize.A4);
        Document document = new Document(pdfDocument);

        Orders orders = orderRepository.findByOrderId(orderId);
        if(orders == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found orders");
        }
        if (orders.getStatus() == Status_Order.CANCELLED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad request");
        }
        User user = userRepository.findByUserId(orders.getUser().getUserId());
        float[] twoColumnWidth = {230f + 150f, 230f};
        float[] threeColumnWidth = {190f, 190f, 190f};
        float[] forColumnWidth = {190f, 190f, 190f, 190f};
        float[] fullWidth = {230f * 3};

        // Empty space between sections
        Paragraph emptyParagraph = new Paragraph("\n");

        Table companyInfoTable = new Table(new float[]{100f, 400f});
        companyInfoTable.addCell(new Cell().add(new Paragraph("HMDrinks")
                        .setFontSize(14f)
                        .setFont(vietnameseFont)
                        .setBold())
                .setBorder(Border.NO_BORDER) // Không hiển thị border
        );
        companyInfoTable.addCell(new Cell().add(new Paragraph("Số 1 Võ Văn Ngân, Thủ Đức, Hồ Chí Minh")
                        .setFontSize(10f)
                        .setFont(vietnameseFont))
                .setBorder(Border.NO_BORDER) // Không hiển thị border
        );
        document.add(companyInfoTable);


         Table mainTable = new Table(twoColumnWidth);
        mainTable.addCell(new Cell().add(new Paragraph("Hóa đơn")
                .setFontSize(18f)
                .setFont(vietnameseFont)
        ));

        Table nestedTable = new Table(new float[]{115f, 115f});
        nestedTable.addCell(createHeaderTextCell("Số hóa đơn :"));
        nestedTable.addCell(createHeaderTextCellValue(String.valueOf(orderId)));
        nestedTable.addCell(createHeaderTextCell("Ngày in hóa đơn:"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        nestedTable.addCell(createHeaderTextCellValue(LocalDateTime.now().format(formatter)));

        mainTable.addCell(new Cell().add(nestedTable).setBorder(Border.NO_BORDER));
        mainTable.setBorder(Border.NO_BORDER);
        document.add(mainTable);
        document.add(emptyParagraph);


        Border dividerBorder = new SolidBorder(new DeviceRgb(169, 169, 169), 2f);
        Table divider = new Table(fullWidth);
        divider.setBorder(dividerBorder);
        document.add(divider);
        document.add(emptyParagraph);

        Table billingShippingTable = new Table(twoColumnWidth);
        billingShippingTable.addCell(createBillingAndShippingCell("Thông tin thanh toán"));
        billingShippingTable.addCell(createBillingAndShippingCell("Thông tin giao hàng"));
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
        addressTable.addCell(createCell10fLeft(String.valueOf(orders.getDateCreated().format(formatter)), false));
        addressTable.addCell(createCell10fLeft(
                user.getStreet() + ", " + user.getWard() + ", " + user.getDistrict() + ", " + user.getCity(), false));

        document.add(addressTable);

        Table additionalInfoTable = new Table(new float[]{230f + 150f});
        additionalInfoTable.addCell(createCell10fLeft("Email", true));
        additionalInfoTable.addCell(createCell10fLeft(user.getEmail(), false));
        additionalInfoTable.addCell(createCell10fLeft("Số điện thoại", true));
        additionalInfoTable.addCell(createCell10fLeft(user.getPhoneNumber(), false));
        document.add(additionalInfoTable.setMarginBottom(10f));

        // Product Section
        Table tableDivider = new Table(fullWidth);
        Border productBorder = new SolidBorder(new DeviceRgb(169, 169, 169), 1f);
        document.add(tableDivider.setBorder(productBorder));

        Paragraph productTitle = new Paragraph("Sản phẩm").setBold().setFont(vietnameseFont);
        document.add(productTitle);

        // Product Table Header
        Table productHeaderTable = new Table(forColumnWidth);
        productHeaderTable.setBackgroundColor(new DeviceRgb(0, 0, 0), 0.7f);
        productHeaderTable.addCell(new Cell().add(new Paragraph("Mô tả")
                .setBold().setFontColor(DeviceRgb.WHITE).setBorder(Border.NO_BORDER).setFont(vietnameseFont)));
        productHeaderTable.addCell(new Cell().add(new Paragraph("Số lượng")
                .setBold().setFontColor(DeviceRgb.WHITE).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER)).setFont(vietnameseFont));
        productHeaderTable.addCell(new Cell().add(new Paragraph("Giá")
                .setBold().setFontColor(DeviceRgb.WHITE).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER)).setFont(vietnameseFont));
        productHeaderTable.addCell(new Cell().add(new Paragraph("Tổng cộng")
                .setBold().setFontColor(DeviceRgb.WHITE).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER).setMarginRight(5f)).setFont(vietnameseFont));
        document.add(productHeaderTable);

        List<Products> productList = new ArrayList<>();
        OrderItem orderItem = orders.getOrderItem();
        List<CartItem> cartItem = cartItemRepository.findByCart_CartId(orderItem.getCart().getCartId());
        for(CartItem cartItem1 : cartItem) {
            ProductVariants productVariants = cartItem1.getProductVariants();
            productList.add(new Products(productVariants.getProduct().getProName() +"-" + productVariants.getSize(), cartItem1.getQuantity(), productVariants.getPrice()));
        }


        Table productTable = new Table(forColumnWidth);
        float totalAmount = 0f;
        for (Products product : productList) {
            Double total = product.getQuantity() * product.getPricePerPiece();
            totalAmount += total;
            productTable.addCell(new Cell().add(new Paragraph(product.getProductName()).setBorder(Border.NO_BORDER).setFont(vietnameseFont)));
            productTable.addCell(new Cell().add(new Paragraph(Integer.toString(product.getQuantity())).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER)));
            productTable.addCell(new Cell().add(new Paragraph(Double.toString(product.getPricePerPiece())).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER)));
            productTable.addCell(new Cell().add(new Paragraph("₫ " + total).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER).setMarginRight(5f)));
        }
        document.add(productTable);

        Paragraph totalTitle = new Paragraph("Tổng kết").setBold().setFont(vietnameseFont);
        document.add(totalTitle);

        Table totalSummaryTable = new Table(threeColumnWidth);

        totalSummaryTable.addCell(new Cell().add(new Paragraph("").setBorder(Border.NO_BORDER)));
        totalSummaryTable.addCell(new Cell().add(new Paragraph("Tiền phí sản phẩm")
                .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT).setFont(vietnameseFont)));
        totalSummaryTable.addCell(new Cell().add(new Paragraph("₫ " + totalAmount)
                .setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT).setMarginRight(15f)));

        totalSummaryTable.addCell(new Cell().add(new Paragraph("").setBorder(Border.NO_BORDER)));
        totalSummaryTable.addCell(new Cell().add(new Paragraph("Tiền phí vân chuyển")
               .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT).setFont(vietnameseFont)));
        totalSummaryTable.addCell(new Cell().add(new Paragraph("₫ " + orders.getDeliveryFee())
                .setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT).setMarginRight(15f)));

        totalSummaryTable.addCell(new Cell().add(new Paragraph("").setBorder(Border.NO_BORDER)));
        totalSummaryTable.addCell(new Cell().add(new Paragraph("Tiền được giảm giá")
                .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT).setFont(vietnameseFont)));
        totalSummaryTable.addCell(new Cell().add(new Paragraph("₫ " + orders.getDiscountPrice())
                .setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT).setMarginRight(15f)));

        totalSummaryTable.addCell(new Cell().add(new Paragraph("").setBorder(Border.NO_BORDER)));
        totalSummaryTable.addCell(new Cell().add(new Paragraph("Tổng cộng")
                .setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT).setFont(vietnameseFont)));
        totalSummaryTable.addCell(new Cell().add(new Paragraph("₫ " + (totalAmount + orders.getDeliveryFee() - orders.getDiscountPrice()))
                .setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT).setMarginRight(15f)));
        document.add(totalSummaryTable);


        document.add(new Paragraph("\n\n"));
        document.add(new Paragraph("Chính sách hoàn trả và đổi trả hàng")
                .setBold().setFontSize(12f).setFont(vietnameseFont).setTextAlignment(TextAlignment.CENTER));

// Return Policy Points
        document.add(new Paragraph("1. Sản phẩm có thể được hoàn trả trong vòng 7 ngày kể từ ngày nhận hàng.")
                .setFontSize(10f).setFont(vietnameseFont));
        document.add(new Paragraph("2. Sản phẩm phải được trả lại trong tình trạng ban đầu, chưa qua sử dụng.")
                .setFontSize(10f).setFont(vietnameseFont));
        document.add(new Paragraph("3. Không chấp nhận trả lại hoặc đổi trả với sản phẩm đã qua sử dụng.")
                .setFontSize(10f).setFont(vietnameseFont));

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
