package com.multiverse;

import com.google.inject.Provides;
import javax.inject.Inject;

import com.multiverse.ws.MultiverseWebSocketListener;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

import java.awt.image.BufferedImage;
import java.util.Arrays;

@Slf4j
@PluginDescriptor(
	name = "Multiverse"
)
public class MultiversePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private MultiverseConfig config;

	private MultiverseWebSocketListener listener;
	private String currentRealm;
	private String websocketServer = "139.162.135.91:7071";
	private String currentlyLoggedInAccount;
	private boolean previouslyLoggedIn;
	private OkHttpClient okClient;

	private WebSocket ws;
	private int modIconsStart = -1;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Example started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
		if (!okClient.dispatcher().executorService().isShutdown()) {
			okClient.dispatcher().executorService().shutdown();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
			onLoggedInGameState();
		} else if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN && previouslyLoggedIn) {
			//this randomly fired at night hours after i had logged off...so i'm adding this guard here.
			if (currentlyLoggedInAccount != null && client.getGameState() != GameState.LOGGED_IN) {
				handleLogout();
			}
		}
	}


	private void onLoggedInGameState() {
		//keep scheduling this task until it returns true (when we have access to a display name)
		clientThread.invokeLater(() ->
		{
			//we return true in this case as something went wrong and somehow the state isn't logged in, so we don't
			//want to keep scheduling this task.
			if (client.getGameState() != GameState.LOGGED_IN) {
				return true;
			}

			final Player player = client.getLocalPlayer();

			//player is null, so we can't get the display name so, return false, which will schedule
			//the task on the client thread again.
			if (player == null) {
				return false;
			}

			String name = player.getName();

			if (name == null) {
				return false;
			}

			if (name.equals("")) {
				return false;
			}
			previouslyLoggedIn = true;

			if (currentlyLoggedInAccount == null) {
				name = Text.sanitize(name);
				handleLogin(name);
			}
			//stops scheduling this task
			return true;
		});
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted commandExecuted)
	{
		switch (commandExecuted.getCommand()) {
			case ":":
				if (!listener.isSocketConnected()) {
					String displayName = client.getLocalPlayer().getName();
					displayName = Text.sanitize(displayName);
					connectToWebsocket(displayName);
				}
				ws.send(String.join(" ", commandExecuted.getArguments()));
				break;
			case "realm":
				if (commandExecuted.getArguments().length > 0) {
					String displayName = client.getLocalPlayer().getName();
					displayName = Text.sanitize(displayName);
					String realm = commandExecuted.getArguments()[0];

					if (realm.equals(currentRealm)) {
						client.addChatMessage(ChatMessageType.FRIENDSCHAT, "Multiverse", String.format("<col=588a12>Already connected to realm \"%s\"</col>", currentRealm), "<img=15>");
					} else {
						if (!currentRealm.equals("Disconnected")) {
							ws.close(1000, null);
							client.addChatMessage(ChatMessageType.FRIENDSCHAT, "Multiverse", String.format("<col=8a121c>Disconnecting from realm \"%s\"</col>", currentRealm), "<img=15>");
						}
						client.addChatMessage(ChatMessageType.FRIENDSCHAT, "Multiverse", String.format("<col=1849ba>Connecting to realm \"%s\"...</col>", realm), "<img=15>");
						connectToWebsocket(displayName, realm);
					}

				} else {
					if (!currentRealm.equals("Disconnected")) {
						client.addChatMessage(ChatMessageType.FRIENDSCHAT, "Multiverse", String.format("<col=588a12>Currently connected to realm \"%s\"</col>", currentRealm), "<img=15>");
					} else {
						client.addChatMessage(ChatMessageType.FRIENDSCHAT, "Multiverse", String.format("<col=8a121c>Disconnected.</col>"), "<img=15>");
					}
				}

			default:
				break;
		}
	}

	public void handleLogin(String displayName) {
		log.info("{} has just logged in!", displayName);

		int iconsStartIndex = loadEmojiIcons();

		if (iconsStartIndex > 0) {
			connectToWebsocket(displayName);
		}
	}

	public void connectToWebsocket(String displayName, String realm) {
		log.debug("Connecting...");

		okClient = new OkHttpClient();

		Request request = new Request.Builder().url(String.format("ws://%s/ws?platform=plugin&name=%s&realm=%s",websocketServer,displayName,realm)).build();
		listener = new MultiverseWebSocketListener(modIconsStart, client, clientThread);

		ws = okClient.newWebSocket(request, listener);
		Thread one = new Thread() {
			public void run() {
				try {
					Thread.sleep(2000);

					clientThread.invokeLater(() -> {
						if (listener.isSocketConnected()) {
							client.addChatMessage(ChatMessageType.FRIENDSCHAT, "Multiverse", String.format("<col=588a12>Connected to realm \"%s\"</col>", realm), "<img=15>");
							currentRealm = realm;
						} else {
							client.addChatMessage(ChatMessageType.FRIENDSCHAT, "Multiverse", String.format("<col=8a121c>Failed to connect to realm %s</col>", realm), "<img=15>");
							currentRealm = "Disconnected";
						}
					});

				} catch(InterruptedException v) {
					log.debug("Unknown Multiverse Error");
				}
			}
		};

		one.start();
	}

	public void connectToWebsocket(String displayName) {
		connectToWebsocket(displayName, config.realm());
	}

	public void handleLogout() {
		log.info("{} is logging out", currentlyLoggedInAccount);
		ws.close(1000, "Client logged off");
	}

	private int loadEmojiIcons()
	{
		final IndexedSprite[] modIcons = client.getModIcons();
		if (modIconsStart != -1 || modIcons == null)
		{
			return 0;
		}

		final Icons[] icons = Icons.values();
		final IndexedSprite[] newModIcons = Arrays.copyOf(modIcons, modIcons.length + icons.length);
		modIconsStart = modIcons.length;

		for (int i = 0; i < icons.length; i++)
		{
			final Icons icon = icons[i];

			try
			{
				final BufferedImage image = icon.loadImage();
				final IndexedSprite sprite = ImageUtil.getImageIndexedSprite(image, client);
				newModIcons[modIconsStart + i] = sprite;
			}
			catch (Exception ex)
			{
				log.warn("Failed to load the sprite for icon " + icon, ex);
			}
		}

		log.debug("Adding emoji icons");
		client.setModIcons(newModIcons);
		return modIconsStart;
	}

	@Provides
	MultiverseConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MultiverseConfig.class);
	}
}
