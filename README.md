# Multiverse
A plugin to communicate in-game with people on other worlds or platforms, currently only supports discord.

## How it works
The plugin works as a basic websocket chat client with rooms which are referred to as "realms" in the plugin.

All messages related to the plugin count as "Chat channel messages" and are being shown under the "Channel" chat tab.

![In game channel tab](https://user-images.githubusercontent.com/55879449/188292324-ddce00cc-43ca-4767-92c1-2e4428bbee4b.png)

## Setup
### Plugin configuration
The plugin config features only one setting, the default realm to connect to:

![Plugin configuration](https://user-images.githubusercontent.com/55879449/188292327-8c26d92f-b043-447b-a337-1b877db0444e.png)


### In game
After logging in you should see this message:

![Connected to realm message](https://user-images.githubusercontent.com/55879449/188292330-e7be7cfb-d803-4b5c-b14b-3372ad69b963.png)

which lets you know in game what realm you are being connected to.

You can change your realm using the `::realm` command

![Changing realm using command](https://i.imgur.com/TbBR5qV.gif)


In order to send a message to the realm you should do  `::: <message>`

![Sending a message to a realm](https://i.imgur.com/46ofubX.gif)


## Connecting Discord
In order to bind a discord channel to a realm you will have to add the following [Discord Bot](https://discord.com/oauth2/authorize?client_id=1010534003520049242&permissions=75776&scope=bot) to your server, and use the `/bind` command in the channel that you wish to bind to the realm.

![Binding discord channel](https://i.imgur.com/QNRFBHW.gif)


## Plugin in action
Players connected to a realm thats bound to a discord channel can now interact with each other!

![Messages in discord](https://user-images.githubusercontent.com/55879449/188292383-0377f941-1213-4ea1-ad0b-9575215632d2.png)

![Messages in game](https://user-images.githubusercontent.com/55879449/188292387-bae34e34-2f53-49a4-9c48-62c7951de833.png)
