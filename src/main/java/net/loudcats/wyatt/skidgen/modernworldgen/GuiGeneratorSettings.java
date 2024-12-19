package net.loudcats.wyatt.skidgen.modernworldgen;

import net.loudcats.wyatt.skidgen.gui.*;
import java.util.ArrayList;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;

// credit, ted80 for OWG (of whose code I have stolen much of for skidgen)
// I am using hardtabs here as my file my choice
public class GuiGeneratorSettings extends GuiScreen {
	private final GuiCreateWorld createWorldGui;
	
	public GuiButton BUTTON_DONE;
	public GuiButton BUTTON_CATEGORY;
	
	public int generatorSelected = -1;
	public ArrayList<GuiGeneratorButton> generators;
	public ArrayList<GuiSettingsButton> settings;

	public boolean decodebool;
	public boolean setremember;
	public int[] rememberSettings;
	
	public boolean hasSettings = false;
	
	public String[] translatedDrawStrings;
	
	public GuiGeneratorSettings(GuiCreateWorld gcw, String gs)
	{
    	createWorldGui = gcw;
    	decodebool = true;

    	translatedDrawStrings = new String[2];
    	translatedDrawStrings[0] = StatCollector.translateToLocal("gui.selectGenerator");
    	translatedDrawStrings[1] = StatCollector.translateToLocal("gui.generatorSettings");
	}

	public void initGui()
	{
        buttonList.add(BUTTON_DONE = new GuiButton(0, width / 2 - 155, height - 24, 150, 20, StatCollector.translateToLocal("gui.done")));
        buttonList.add(new GuiButton(1, width / 2 + 5, height - 24, 150, 20, StatCollector.translateToLocal("gui.cancel")));

		//System.out.println("INIT");
		
		if(false)
		{
			decodebool = false;
			decodeString(createWorldGui.field_146334_a);
		}
		else
		{
			//createList();
			
	        settings = new ArrayList<GuiSettingsButton>();
			selectGenerator();

            //hasSettings = getSettings(this);

	        for(int s = 0; s < settings.size(); s++)
	        {
	            buttonList.add(settings.get(s).button);
	        }

	        dependencies();

	        if(setremember)
	        {
	            for(int rs = 0; rs < settings.size(); rs++)
	            {
	                settings.get(rs).setOldValue(rememberSettings[rs]);
	            }
	            setremember = false;
	        }
		}
	}

	protected void actionPerformed(GuiButton button)
	{//idk if we need this whole method, might be able to yoink it
        if (button.id == 0) //DONE
        {
        	createWorldGui.field_146334_a = createString();
        	mc.displayGuiScreen(this.createWorldGui); 
        }
        else if (button.id == 1) //CANCEL
        {
        	mc.displayGuiScreen(this.createWorldGui);
        }
        else if (button.id >= 10 && button.id < 20)
        {
        	for(int i = 0; i < generators.size(); i++)
        	{
        		generators.get(i).button.enabled = true;
        		if(generators.get(i).button.id == button.id)
        		{
            		generators.get(i).button.enabled = false;
        			generatorSelected = generators.get(i).generatorID;
        		}
        	}
    		selectGenerator();
        }
        else if (button.id >= 20 && button.id < 30)
        {
        	for(int i = 0; i < settings.size(); i++)
        	{
        		if(settings.get(i).button.id == button.id)
        		{
        			settings.get(i).click();
        		}
        	}
        	dependencies();
        }
	}
	
	public void drawScreen(int par1, int par2, float par3)
	{
		drawDefaultBackground();//maybe do a background out of like deepslate bcz caves n cliffs idfk
		
		//title
		String title = "SkidGen -- Modern Caves \'n Cliffs Worldgen Backport";
		drawString(fontRendererObj, title, (int) Math.floor(width / 2) - (int) Math.floor(fontRendererObj.getStringWidth(title) / 2), 10, 16777215);
		
		//select generator
    	//drawString(fontRendererObj, translatedDrawStrings[0], width / 2 - 155 + 1, 40, 10526880);

   		drawString(fontRendererObj, translatedDrawStrings[1], width / 2 -155 + 1, 40, 10526880);
    	
    	drawString(fontRendererObj, "Server config: ", width / 2 - 155 + 1, 230, 16777215);
    	drawString(fontRendererObj, "level-type=1.18", width / 2 - 155 + 1, 242, 10526880);
    	drawString(fontRendererObj, "generator-settings=" + createString(), width / 2 - 155 + 1, 252, 10526880);
		
		super.drawScreen(par1, par2, par3);
	}
	
	/*public void createList()
	{
		if(generators != null)
		{
			for(int i = 0; i < generators.size(); i++)
			{
				buttonList.remove(generators.get(i).button);
			}
		}
		
		generators = new ArrayList<GuiGeneratorButton>();
		int count = 0;
		for(int g = 0; g < GeneratorType.generatortypes.length; g++)
		{
			if(GeneratorType.generatortypes[g] != null)
			{
				generators.add(new GuiGeneratorButton(StatCollector.translateToLocal("skidgen." + GeneratorType.generatortypes[g].GetName()), g, count + 10, 50 + (20 * count), width));
				buttonList.add(generators.get(generators.size() - 1).button);
				count++;
			}
		}
	}*/
	
	public void dependencies()
	{
		for(int i = 0; i < settings.size(); i++)
		{
    		if(settings.get(i).dependencie > -1)
    		{
    			settings.get(i).button.visible = true;
    		}
		}
	}
	
	public void selectGenerator()
	{/*
		if(generatorSelected > -1)
		{
			BUTTON_DONE.enabled = true;
		}
		else
		{
			BUTTON_DONE.enabled = false;
		}
		
		if(settings != null)
		{
			for(int i = 0; i < settings.size(); i++)
			{
				buttonList.remove(settings.get(i).button);
			}
		}
		settings = new ArrayList<GuiSettingsButton>();
		
		if(generatorSelected > -1)
		{
			//hasSettings = GeneratorType.generatortypes[generatorSelected].getSettings(this);
		}

		for(int s = 0; s < settings.size(); s++)
		{
			buttonList.add(settings.get(s).button);
		}
		
		dependencies();
		
		if(setremember)
		{
			for(int rs = 0; rs < settings.size(); rs++)
			{
				settings.get(rs).setOldValue(rememberSettings[rs]);
			}
			setremember = false;
		}*/
		//gui.settings.add(new GuiSettingsButton(new String[]{StatCollector.translateToLocal("owg.setting.theme") + ": " + StatCollector.translateToLocal("owg.theme.default"), StatCollector.translateToLocal("owg.setting.theme") + ": " + StatCollector.translateToLocal("owg.theme.hell"), StatCollector.translateToLocal("owg.setting.theme") + ": " + StatCollector.translateToLocal("owg.theme.paradise"), StatCollector.translateToLocal("owg.setting.theme") + ": " + StatCollector.translateToLocal("owg.theme.woods"), StatCollector.translateToLocal("owg.setting.theme") + ": " + StatCollector.translateToLocal("owg.theme.snow")}, new int[]{0, 1, 2, 3, 4}, 20, 50, gui.width));
        //gui.settings.add(new GuiSettingsButton(new String[]{StatCollector.translateToLocal("owg.setting.type") + ": " + StatCollector.translateToLocal("owg.type.island"), StatCollector.translateToLocal("owg.setting.type") + ": " + StatCollector.translateToLocal("owg.type.floating"), StatCollector.translateToLocal("owg.setting.type") + ": " + StatCollector.translateToLocal("owg.type.inland")}, new int[]{0, 1, 2}, 21, 70, gui.width));
        //gui.settings.add(new GuiSettingsSlider(new String[]{StatCollector.translateToLocal("skidgen.setting.one") + ": " + StatCollector.translateToLocal("owg.setting.small"), StatCollector.translateToLocal("owg.setting.size") + ": " + StatCollector.translateToLocal("owg.setting.default"), StatCollector.translateToLocal("owg.setting.size") + ": " + StatCollector.translateToLocal("owg.setting.large")}, new int[]{0, 1, 2}, 1, 22, 90, gui.width, 21, new int[]{0, 1}));
		// ikikik i should use localization strings but pain
        settings.add(new GuiSettingsSlider(new String[]{StatCollector.translateToLocal("Depth: Amplified"), StatCollector.translateToLocal("Depth: Default"), StatCollector.translateToLocal("Depth") + ": 1", StatCollector.translateToLocal("Depth") + ": 2", StatCollector.translateToLocal("Depth") + ": 3"}, new int[]{-2, -1, 1, 2, 3}, 1, -24, 90, (int)(width / 2.5), 21, new int[]{1}));
        settings.add(new GuiSettingsSlider(new String[]{StatCollector.translateToLocal("Scale: Amplified"), StatCollector.translateToLocal("Scale: Default"), StatCollector.translateToLocal("Scale") + ": 1", StatCollector.translateToLocal("Scale") + ": 2", StatCollector.translateToLocal("Scale") + ": 3"}, new int[]{-2, -1, 1, 2, 3}, 1, -24, 112, (int)(width / 2.5), 22, new int[]{1}));
        //gui.settings.add(new GuiSettingsButton(new String[]{StatCollector.translateToLocal("owg.setting.dungeon") + ": " + StatCollector.translateToLocal("owg.setting.on"), StatCollector.translateToLocal("owg.setting.dungeon") + ": " + StatCollector.translateToLocal("owg.setting.end"), StatCollector.translateToLocal("owg.setting.dungeon") + ": " + StatCollector.translateToLocal("owg.setting.off")}, new int[]{0, 1, 2}, 24, 130, gui.width, 21, new int[]{1}));
	}
	
	public void decodeString(String decodestring)
	{//idk if we need this method either
		String[] genstring = decodestring.split("#");
		String[] gensettings = new String[0];
		if(genstring.length > 1 && genstring[1].length() > 0)
		{
			gensettings = genstring[1].split("#");
		}
		else
		{
			gensettings = new String[0];
		}
		
		//int n = DecodeGeneratorString.getGeneratorIDFromName(genstring[0]);
		/*
		int n = 3;
		if(n > -1)
		{
			createList();
			generatorSelected = n;
			
        	for(int i = 0; i < generators.size(); i++)
        	{
        		generators.get(i).button.enabled = true;
        		if(generators.get(i).generatorID == generatorSelected)
        		{
            		generators.get(i).button.enabled = false;
        		}
        	}
			selectGenerator();
			
			for(int i = 0; i < settings.size(); i++)
			{
				if(i < gensettings.length)
				{
					settings.get(i).setOldValue(Integer.parseInt(gensettings[i]));
				}
			}
		}
		else
		{
			createList();
			generatorSelected = -1;
			selectGenerator();
		}
		*/
	}
	
	public String createString()
	{
		//String genstring = GeneratorType.generatortypes[generatorSelected].GetName() + "#";
		String genstring = "";
		for(int s = 0; s < settings.size(); s++)
		{
			genstring += s == 0 ? "" : "#";
			//genstring += settings.get(s).valuearray[settings.get(s).selected];
			genstring += settings.get(s).selected;
		}

		return genstring;
	}
}
