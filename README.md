Bruce's Unsecure Mail Sink
==========================

BUMSink is somewhere between a mail sink and a mail server. It's intended
purpose is to allow developers to test software that sends email as part of
its operation while avoiding the risk of accidentally emailing real customers.

t runs as a service and accepts email from any SMTP client and saves them to
the file system. It exposes those same emails to any POP3 client.

What it does not do is forward emails to any other SMTP service.

It provides an SMTP service roughly as defined in
  http://tools.ietf.org/html/rfc5321
and a POP3 service roughly as defined in
  http://www.ietf.org/rfc/rfc1939.txt

I say roughly, as conformance to the standards is only just enough to
(hopefully) not break SMTP and POP3 clients.

The SMTP service accepts any email from any sender for any destination.

The POP3 service accepts any login details and will return any and all emails
currently in the store to the POP3 client.

Neither service currently supports any kind of transport layer security.

It should go without saying that this is NOT a safe or secure piece of
software by any measure. Please don't run it outside of well-secured internal
development or testing network.


USAGE:
======
1. Unzip bumsink.zip. If you're reading this you've probably already done that.

2. Look in the directory you've just created called, "bumsink". You'll find
   this README and four other files.

3. Edit bumsink.properties. There are nine properties:
   Six of them correspond to arguments to the constructor for
   java.net.ServerSocket for the SMTP and POP3 services. You're smart enough
   to work them out.
   "so.timeout" also applies to server sockets, but one value is shared
   between both services.
   "mail.dir" is the name of the directory email is saved to - one email per
   file. If the path doesn't start with a slash, it is relative to the working
   directory of the java process. If you run it using "bumsink.sh", that will
   be the directory where the script is.
   Set "debug = true" to see the messages the client and server are sending to
   each other in the log file.

4. Run bumsink.sh:
   You should see the usage message;

       ./bumsink.sh {start|stop|restart|force-reload|status}

   Start the services with "./bumsink.sh start", stop them with, "./bumsink.sh
   stop" etc.
   It is designed to run as an init script - don't copy it to /etc/init.d
   however. Just provide a soft link. It also runs as root. Maybe you can fix
   that up, I can't bothered right now.

   All logging goes to stdout/stderr and ends up in "bumsink/bumsink.log"
   courtesy of bumsink.sh.
