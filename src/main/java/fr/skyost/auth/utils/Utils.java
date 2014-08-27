package fr.skyost.auth.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skyost.auth.AuthPlugin;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BookMeta;

@SuppressWarnings("deprecation")
public class Utils {
	
	public static final boolean isCorrect(final String password, final String truePassword) throws NoSuchAlgorithmException {
		String blankPassword = "";
		final String algorithm = AuthPlugin.config.PasswordAlgorithm.toUpperCase();
		switch(algorithm) {
		case "CHAR":
			for(int i = 0; i < password.length(); i++)  {
				int c = password.charAt(i) ^ 48;  
				blankPassword += (char)c; 
			}
			break;
		case "PLAIN":
			blankPassword = password;
			break;
		case "MD2":
		case "MD5":
		case "SHA-1":
		case "SHA-256":
		case "SHA-384":
		case "SHA-512":
			MessageDigest md = MessageDigest.getInstance(algorithm);
			byte[] array = md.digest(password.getBytes());
			StringBuffer sb = new StringBuffer();
			for(int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
			}
			blankPassword = sb.toString();
			break;
		}
		if(blankPassword.equals(truePassword)) {
			return true;
		}
		return false;
	}
	
	public static final String encrypt(final String string) throws NoSuchAlgorithmException {
		String blankString = "";
		final String algorithm = AuthPlugin.config.PasswordAlgorithm.toUpperCase();
		switch(algorithm) {
		case "CHAR":
			for(int i = 0; i < string.length(); i++)  {
				int c = string.charAt(i) ^ 48;  
				blankString += (char)c; 
			}
			break;
		case "PLAIN":
			blankString = string;
			break;
		case "MD2":
		case "MD5":
		case "SHA-1":
		case "SHA-256":
		case "SHA-384":
		case "SHA-512":
			MessageDigest md = MessageDigest.getInstance(algorithm);
			byte[] array = md.digest(string.getBytes());
			StringBuffer sb = new StringBuffer();
			for(int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
			}
			blankString = sb.toString();
			break;
		}
		return blankString;
	}
	
	public static final String LocationToString(final Location loc) {
		return loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ();
	}
	
	public static final Location StringToLocation(final String loc) {
		String[] arrayLoc = loc.split(":");
		return new Location(Bukkit.getWorld(arrayLoc[0]), Double.parseDouble(arrayLoc[1]), Double.parseDouble(arrayLoc[2]), Double.parseDouble(arrayLoc[3]));
	}
	
	public static String InventoryToString(Inventory invInventory) {
        String serialization = invInventory.getSize() + ";";
        for(int i = 0; i < invInventory.getSize(); i++) {
        	try {
	            ItemStack is = invInventory.getItem(i);
	            if(is != null) {
	                String serializedItemStack = new String();
					String isType = String.valueOf(is.getType().getId());
	                serializedItemStack += "t@" + isType;
	                if(is.getDurability() != 0) {
	                    String isDurability = String.valueOf(is.getDurability());
	                    serializedItemStack += ":d@" + isDurability;
	                }
	                if(is.getAmount() != 1) {
	                    String isAmount = String.valueOf(is.getAmount());
	                    serializedItemStack += ":a@" + isAmount;
	                }
	                Map<Enchantment,Integer> isEnch = is.getEnchantments();
	                if(isEnch.size() > 0) {
	                    for(Entry<Enchantment,Integer> ench : isEnch.entrySet()) {
	                        serializedItemStack += ":e@" + ench.getKey().getId() + "@" + ench.getValue();
	                    }
	                }
	                serialization += i + "#" + serializedItemStack + ";";
	            }
        	}
        	catch(IllegalArgumentException ex) {
        		ex.printStackTrace();
        	}
        }
        return serialization;
    }
   
    public static Inventory StringToInventory(String invString) {
        String[] serializedBlocks = invString.split(";");
        String invInfo = serializedBlocks[0];
        Inventory deserializedInventory = Bukkit.getServer().createInventory(null, Integer.valueOf(invInfo));
        for(int i = 1; i < serializedBlocks.length; i++) {
        	try {
	            String[] serializedBlock = serializedBlocks[i].split("#");
	            int stackPosition = Integer.valueOf(serializedBlock[0]);
	            if(stackPosition >= deserializedInventory.getSize()) {
	                continue;
	            }
	            ItemStack is = null;
	            Boolean createdItemStack = false;
	            String[] serializedItemStack = serializedBlock[1].split(":");
	            for(String itemInfo : serializedItemStack) {
	                String[] itemAttribute = itemInfo.split("@");
	                if (itemAttribute[0].equals("t")) {
	                    is = new ItemStack(Material.getMaterial(Integer.valueOf(itemAttribute[1])));
	                    createdItemStack = true;
	                }
	                else if (itemAttribute[0].equals("d") && createdItemStack) {
	                    is.setDurability(Short.valueOf(itemAttribute[1]));
	                }
	                else if (itemAttribute[0].equals("a") && createdItemStack) {
	                    is.setAmount(Integer.valueOf(itemAttribute[1]));
	                }
	                else if (itemAttribute[0].equals("e") && createdItemStack) {
	                    is.addEnchantment(Enchantment.getById(Integer.valueOf(itemAttribute[1])), Integer.valueOf(itemAttribute[2]));
	                }
	            }
	            deserializedInventory.setItem(stackPosition, is);
        	}
        	catch(IllegalArgumentException ex) {
                    ex.printStackTrace();
        	}
        }
        return deserializedInventory;
    }
    
    public static List<ItemStack> getFirstJoinKit() {
        List<ItemStack> kit = new ArrayList<ItemStack>();
        for (String s : AuthPlugin.config.Kits.keySet()) {

            Material mat = Material.AIR;
            try {
                mat = Material.getMaterial(s.toUpperCase());
            } catch (Exception e) {
                System.out.println(e.getStackTrace().toString());
            }

            int amount = AuthPlugin.config.Kits.get(s);

            ItemStack is = new ItemStack(mat, amount);
            kit.add(is);
        }
        return kit;
    }

    public static List<ItemStack> getWrittenBooks(Player viewingPlayer, AuthPlugin plugin) {
        List<ItemStack> books = new ArrayList<ItemStack>();
        for (String file : AuthPlugin.config.Books) {
            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta bm = (BookMeta) book.getItemMeta();
            File f = new File(plugin.getDataFolder() + "/" + file);
            try {
                BufferedReader br = new BufferedReader(new FileReader(f));
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();
                int i = 0;

                while (line != null) {
                    i++;
                    line = replaceVariables(line, viewingPlayer);
                    if (i != 1 && i != 2) {
                        if (line.equalsIgnoreCase("/newpage") || line.equalsIgnoreCase("/np")) {
                            bm.addPage(sb.toString());
                            sb = new StringBuilder();
                        } else {
                            sb.append(translateColors(line));
                            sb.append("\n");
                        }
                    } else {
                        if (i == 1) {
                            bm.setTitle(translateColors(line));
                        }
                        if (i == 2) {
                            bm.setAuthor(translateColors(line));
                        }
                    }
                    line = br.readLine();
                }

                br.close();
                bm.addPage(sb.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

            book.setItemMeta(bm);
            books.add(book);
        }
        return books;
    }
    
    public static String replaceVariables(String string, Player player) {
        string = string.replace("%player_name", player.getName());
        string = string.replace("%player_display_name", player.getDisplayName());
        string = string.replace("%player_uuid", player.getUniqueId().toString());
        string = string.replace("%new_line", "\n");
        return translateColors(string);
    }

    public static String replaceVariables(String string, Player player, String reason) {
        return replaceVariables(string.replace("%reason", reason), player);
    }

    public static String translateColors(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }


	
}
