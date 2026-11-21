import nodemailer from "nodemailer";

export interface ProductEnquiryEmail {
  productId: number;
  productTitle: string;
  name: string;
  email: string;
  message: string;
  receivedAt: string;
}

const smtpHost = process.env.SMTP_HOST;
const transporter = smtpHost
  ? nodemailer.createTransport({
      host: smtpHost,
      port: Number(process.env.SMTP_PORT ?? 1025),
      secure: false,
    })
  : null;

export async function sendProductEnquiry(enquiry: ProductEnquiryEmail): Promise<void> {
  if (!transporter) {
    console.info("Enquiry received (local log capture):", JSON.stringify(enquiry));
    return;
  }

  await transporter.sendMail({
    from: process.env.MAIL_FROM ?? "catalogue@example.test",
    to: process.env.SALES_INBOX ?? "sales@example.test",
    replyTo: enquiry.email,
    subject: `Product enquiry: ${enquiry.productTitle}`,
    text: [
      `Product: ${enquiry.productTitle} (ID ${enquiry.productId})`,
      `From: ${enquiry.name} <${enquiry.email}>`,
      `Received: ${enquiry.receivedAt}`,
      "",
      enquiry.message || "No message provided.",
    ].join("\n"),
  });
}
