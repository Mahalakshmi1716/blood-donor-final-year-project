from email_service import send_email


send_email(
    to_email="mahapalani1716@gmail.com",
    to_name="Test User",
    subject="Blood Donor Finder - Test Email",
    message="""
    <h2>Blood Donor Finder</h2>

    <p>This is a test email from the Blood Donor Finder backend.</p>

    <p>Brevo email integration is working successfully.</p>

    <p>❤️ Thank you</p>
    """
)