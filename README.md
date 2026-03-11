PART 2 - Meta Agent Bot

This project implements a Meta-Agent platform for building customer service AI agents.
The idea is that a Customer Service Manager, who may not be a developer, can configure and improve an AI support agent through a simple interface.
A.	First, the manager creates an agent that will be used to do following tasks.
B.	Then the manager uploads support documents such as FAQs, policies, or standard operating procedures. These documents form the knowledge base for the agent.
Format or a template of this document would be: 
Q: How do I reset my password?
A: Click "Forgot Password" on the login screen, enter your email, and follow the link sent to your inbox.
Q: What are the operating hours?
A: We are open Monday-Friday, 9 AM to 5 PM EST.
C.	 When a user asks a question, the system searches the uploaded documents and returns the most relevant answer. The agent performs simple semantic matching by comparing words in the question with document content.
D.	If the agent provides an incorrect answer, the manager can submit feedback with the correct answer. The system stores this feedback.
E.	The manager can then apply AutoFix, which converts the feedback into a correction patch. The next time a similar question is asked, the agent returns the corrected answer.
This creates a human-in-the-loop self-improving system, where the customer service manager continuously improves the agent without needing to make a change to the system code.
The system is implemented using Spring Boot for the backend, an H2 database for storage, and a simple HTML/JavaScript frontend for interaction. 

Here are some validation screenshots: 
1.	Launch the application using http://localhost:8080/ after we start the spring boot application. 
2.	Create an agent. 
   

3.	Upload a template message with frequently asked Q and A.
   

4.	Ask the agent with queries in uploaded document. 

 

5.	Queries not in document.
 
6.	Submit a feedback
 
7.	Fixed answer can be submitted to autofix. 
 
8.	Fixed answer is replied when asked with same question.
 

