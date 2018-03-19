package com.example.androidthings.myproject;

import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public final class HttpdServer
    extends NanoHTTPD
{

  private static final int PORT = 8888;

  public interface OnFireTriggerListener
  {

    void onFireTriggered();

    void onSetTriggered();
  }

  private OnFireTriggerListener listener;

  public HttpdServer(OnFireTriggerListener listener)
  {
    super(PORT);
    this.listener = listener;
  }

  @Override
  public Response serve(IHTTPSession session)
  {
    Map<String, List<String>> parameters = session.getParameters();
    if (parameters.get("fire") != null)
    {
      listener.onFireTriggered();
    }
    else if (parameters.get("set") != null)
    {
      listener.onSetTriggered();
    }

    String html =
        "<html>" +
            "<head>" +
            "<script type=\"text/javascript\">" +
            "  function fire() { window.location = '?fire=true'; }" +
            "  function set() { window.location = '?set=true'; }" +
            "</script></head>" +
            "<body>" +
            "  <button style=\"width: 50%; height: 100%; font-size: 4em;\" onclick=\"fire();\">FIRE!</button>"
            + "<button style=\"width: 50%; height: 100%; font-size: 4em;\" onclick=\"set();\">Prepare Arm!</button>" +
            "</body></html>";

    return newFixedLengthResponse(html);
  }
}
