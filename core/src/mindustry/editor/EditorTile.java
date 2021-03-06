package mindustry.editor;

import arc.func.*;
import arc.util.ArcAnnotate.*;
import mindustry.content.*;
import mindustry.editor.DrawOperation.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.modules.*;

import static mindustry.Vars.*;

public class EditorTile extends Tile{

    public EditorTile(int x, int y, int floor, int overlay, int wall){
        super(x, y, floor, overlay, wall);
    }

    @Override
    public void setFloor(@NonNull Floor type){
        if(state.isGame()){
            super.setFloor(type);
            return;
        }

        if(type instanceof OverlayFloor){
            //don't place on liquids
            if(!floor.isLiquid){
                setOverlayID(type.id);
            }
            return;
        }

        if(floor == type && overlayID() == 0) return;
        if(overlayID() != 0) op(OpType.overlay, overlayID());
        if(floor != type) op(OpType.floor, floor.id);
        super.setFloor(type);
    }

    @Override
    public void updateOcclusion(){
        super.updateOcclusion();

        ui.editor.editor.renderer().updatePoint(x, y);
    }

    @Override
    public void setBlock(Block type, Team team, int rotation){
        if(state.isGame()){
            super.setBlock(type, team, rotation);
            return;
        }

        op(OpType.block, block.id);
        if(rotation != 0) op(OpType.rotation, (byte)rotation);
        if(team() != Team.derelict) op(OpType.team, team().id);
        super.setBlock(type, team, rotation);
    }

    @Override
    public void setTeam(Team team){
        if(state.isGame()){
            super.setTeam(team);
            return;
        }

        if(getTeamID() == team.id) return;
        op(OpType.team, getTeamID());
        super.setTeam(team);
    }

    @Override
    public void rotation(int rotation){
        if(state.isGame()){
            super.rotation(rotation);
            return;
        }

        if(rotation == rotation()) return;
        op(OpType.rotation, rotation());
        super.rotation(rotation);
    }

    @Override
    public void setOverlay(Block overlay){
        if(state.isGame()){
            super.setOverlay(overlay);
            return;
        }

        if(floor.isLiquid) return;
        if(overlay() == overlay) return;
        op(OpType.overlay, this.overlay.id);
        super.setOverlay(overlay);
    }

    @Override
    protected void preChanged(){
        super.preChanged();
    }

    @Override
    public void recache(){
        if(state.isGame()){
            super.recache();
        }
    }
    
    @Override
    protected void changeEntity(Team team, Prov<Building> entityprov){
        if(state.isGame()){
            super.changeEntity(team, entityprov);
            return;
        }

        build = null;

        if(block == null) block = Blocks.air;
        if(floor == null) floor = (Floor)Blocks.air;
        
        Block block = block();

        if(block.hasEntity()){
            build = entityprov.get().init(this, team, false);
            build.cons(new ConsumeModule(build));
            if(block.hasItems) build.items = new ItemModule();
            if(block.hasLiquids) build.liquids(new LiquidModule());
            if(block.hasPower) build.power(new PowerModule());
        }
    }

    private void op(OpType type, short value){
        ui.editor.editor.addTileOp(TileOp.get(x, y, (byte)type.ordinal(), value));
    }
}
