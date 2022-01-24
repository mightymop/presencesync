package org.igniterealtime.openfire.plugin.presencesync;

import java.io.File;
import java.util.Collection;

import org.dom4j.Node;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.roster.Roster;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.SystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;
import org.xmpp.packet.Presence.Show;


/**
 * An Openfire plugin that syncs presence between ressources.
 *
 * @author 
 */
public class PresenceSyncPlugin implements Plugin, PacketInterceptor
{
    private static final Logger Log = LoggerFactory.getLogger( PresenceSyncPlugin.class );

    private InterceptorManager interceptorManager;

    public static final SystemProperty<Boolean> XMPP_PRESENCESYNC_ENABLED = SystemProperty.Builder.ofType(Boolean.class)
            .setKey("xmpp.presencesync.enabled")
            .setPlugin( "presencesync" )
            .setDefaultValue(false)
            .setDynamic(true)
            .build();

    @Override
    public void initializePlugin( PluginManager manager, File pluginDirectory )
    {
        SystemProperty.removePropertiesForPlugin("presencesync");
        Log.info("Initialize PresenceSync Plugin enabled:"+XMPP_PRESENCESYNC_ENABLED.getDisplayValue());
        this.interceptorManager = InterceptorManager.getInstance();
        this.interceptorManager.addInterceptor(this);
    }

    @Override
    public void destroyPlugin()
    {
        interceptorManager.removeInterceptor(this);
    }

    @Override
    public void interceptPacket(Packet arg0, Session arg1, boolean incoming, boolean processed) throws PacketRejectedException {
        if (incoming && !processed && arg0 instanceof Presence && XMPP_PRESENCESYNC_ENABLED.getValue())
        {
            Presence p = (Presence)arg0;
            if (p.getFrom()!=null&&p.getFrom().getDomain().equals(XMPPServer.getInstance().getServerInfo().getXMPPDomain())&&p.getType()==null)
            {
                String username = arg1.getAddress().getNode();
                Collection<ClientSession> list = XMPPServer.getInstance().getSessionManager().getSessions(username);

                Node show = p.getElement().selectSingleNode("//*[local-name()='show']"); //ohne = online, sonst: chat, away, xa, dnd
                Node status = p.getElement().selectSingleNode("//*[local-name()='status']");

                Roster r = null;

                try {
                    r = XMPPServer.getInstance().getUserManager().getUser(username).getRoster();
                } catch (UserNotFoundException e) {
                    Log.error("User {} not found while sync presence.",username);
                }

                for (ClientSession itm : list)
                {
                    if (itm!=arg1)
                    {
                        if (show!=null&&show.getText()!=null&&show.getText().trim().length()>0)
                        {
                            String sshow = show.getText().trim().toLowerCase();

                            if (itm!=arg1)
                            {
                                if (sshow.equals("away"))
                                {
                                    itm.getPresence().setShow(Show.away);
                                }
                                else
                                    if (sshow.equals("xa"))
                                    {
                                        itm.getPresence().setShow(Show.xa);
                                    }
                                    else
                                        if (sshow.equals("chat"))
                                        {
                                            itm.getPresence().setShow(Show.chat);
                                        }
                                        else
                                            if (sshow.equals("dnd"))
                                            {
                                                itm.getPresence().setShow(Show.dnd);
                                            }
                            }
                        }
                        else {
                            itm.getPresence().setShow(null);
                        }
                        if (status!=null&&status.getText()!=null&&status.getText().trim().length()>0)
                        {
                            itm.getPresence().setStatus(status.getText().trim());
                        }
                        else {
                            itm.getPresence().setStatus(null);
                        }

                        r.broadcastPresence(itm.getPresence());
                    }
                }
            }
        }
    }
}
