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