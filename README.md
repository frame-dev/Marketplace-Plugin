# Marketplace Plugin for Trial

## Description

This is a simple Marketplace Plugin. <br>
Full support of NBT Tags and enchantments.

### Requirements
- Spigot Server 1.21.4
- Java 21 or higher
- MongoDB for database storage

### Commands
This Command is required to set a Item for sale in the Marketplace / Blackmarket
```
/sell <price>
```
This command allows you to take a look at the Marketplace and update your Items
```
/marketplace
```
This command allows you to buy stuff from the Blackmarket
```
/blackmarket
```
This command displays your transaction History
```
/transactions
```
This command is intended for Admins, this command allows you to delete Items from the Market rename and change price
```
/marketplace-admin
```

### Permissions
- `marketplace.sell`: Permission to use the `/sell` command.
- `marketplace.marketplace`: Permission to access the Marketplace.
- `marketplace.blackmarket`: Permission to access the Blackmarket.
- `marketplace.history`: Permission to view transaction history.

### Features
- **Marketplace GUI**: A user-friendly interface for buying and selling items.
- **Blackmarket GUI**: A separate interface for Blackmarket transactions.
- **Confirmation GUI**: Ensures secure transactions for buying items.
- **MongoDB Integration**: Stores transaction and item data.
- **NBT Tag Support**: Full support for custom item data and enchantments.

### Installation
1. Download the latest version of the plugin from the [releases page](https://github.com/frame-dev/Marketplace-Plugin/releases/tag/1.0-RELEASE)
2. Place the downloaded `.jar` file into the `plugins` folder of your Spigot server.
3. Restart the server.
4. Configure the plugin settings in the `config.yml` file located in the `plugins/Marketplace` directory.
5. Ensure MongoDB is running and properly configured in the `config.yml`.
6. Use the commands listed above to interact with the plugin.

## Changelogs

### 1.0-SNAPSHOT
- Initial release
- Added basic functionality
- Added basic tests
- Basic Marketplace GUI
- Basic Blackmarket GUI
- Basic Confirmation GUI for buy
- Basic Sell Command
- Transaction implemented

### 1.0-RELEASE
- Full release with all features and most bugs removed.
- Added MongoDB integration.
- Improved GUI navigation for Marketplace and Blackmarket.
- Enhanced error handling for transactions.